package com.final_project.product_service.scheduler;

import com.final_project.product_service.dto.request.ProductRequest;
import com.final_project.product_service.dto.response.DummyProductItem;
import com.final_project.product_service.dto.response.DummyProductResponse;
import com.final_project.product_service.repository.ProductRepository;
import com.final_project.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final RestClient restClient;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Value("${app.scheduler.dummy-json.enabled:true}")
    private boolean enabled;

    @Value("${app.scheduler.dummy-json.url:https://dummyjson.com/products}")
    private String dummyJsonUrl;

    @Value("${app.scheduler.dummy-json.limit:5}")
    private int limit;

    private final AtomicInteger currentSkip = new AtomicInteger(0);

    /**
     * Menjalankan sinkronisasi data dari DummyJSON secara terjadwal.
     * Default cron: setiap 5 menit ("0 * / 5 * * * *")
     */
    @Scheduled(cron = "${app.scheduler.dummy-json.cron:0 */5 * * * *}")
    public void scheduledSync() {
        if (!enabled) {
            log.info("[Scheduler] Product sync scheduler dinonaktifkan.");
            return;
        }

        log.info("[Scheduler] Memulai sinkronisasi berkala data dari DummyJSON...");
        syncProductsFromDummyJson();
    }

    /**
     * Mengambil data dari DummyJSON dan menyimpannya ke database.
     * Dapat juga dipanggil secara manual jika dibutuhkan.
     *
     * @return jumlah produk baru yang berhasil disimpan
     */
    public int syncProductsFromDummyJson() {
        int skip = currentSkip.get();
        URI uri = UriComponentsBuilder.fromUriString(dummyJsonUrl)
                .queryParam("limit", limit)
                .queryParam("skip", skip)
                .build()
                .toUri();

        log.info("[Scheduler] Mengirim GET request ke: {} (limit: {}, skip: {})", uri, limit, skip);

        try {
            DummyProductResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(DummyProductResponse.class);

            if (response == null || response.products() == null || response.products().isEmpty()) {
                log.info("[Scheduler] Tidak ada produk yang diterima dari DummyJSON.");
                return 0;
            }

            int savedCount = 0;
            int skippedCount = 0;

            for (DummyProductItem item : response.products()) {
                String kalimat = item.title();
                if (kalimat == null || kalimat.trim().isEmpty()) {
                    continue;
                }

                // Cek apakah produk dengan kalimat ini sudah tersimpan di database
                if (productRepository.existsByKalimat(kalimat.trim())) {
                    log.info("[Scheduler] Produk '{}' sudah ada di database, lewati.", kalimat);
                    skippedCount++;
                    continue;
                }

                // Simpan produk baru via ProductService
                // Method ini otomatis menghitung totalHuruf, frekuensi huruf, dan membersihkan Redis cache 'products'
                productService.createProduct(new ProductRequest(kalimat.trim()));
                savedCount++;
                log.info("[Scheduler] Berhasil menyimpan produk baru: '{}'", kalimat);
            }

            log.info("[Scheduler] Selesai: {} produk baru disimpan, {} dilewati (duplikat).", savedCount, skippedCount);

            // Geser nilai skip agar pemanggilan berikutnya mengambil batch data baru
            int total = response.total() != null ? response.total() : 0;
            int nextSkip = skip + limit;
            if (total > 0 && nextSkip >= total) {
                currentSkip.set(0);
                log.info("[Scheduler] Mencapai batas total data ({}), reset skip ke 0.", total);
            } else {
                currentSkip.set(nextSkip);
            }

            return savedCount;

        } catch (Exception e) {
            log.error("[Scheduler] Terjadi kesalahan saat sinkronisasi data DummyJSON: {}", e.getMessage(), e);
            return 0;
        }
    }
}
