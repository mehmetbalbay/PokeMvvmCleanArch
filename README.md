# PokemonApp - Clean Architecture Örneği

Bu proje, modern Android geliştirme prensiplerini ve Clean Architecture yapısını görselleştirmek için geliştirilmiş bir örnek uygulamadır. Pokemon API kullanarak Pokemon bilgilerini listeleyen, detaylarını gösteren ve favori Pokemon'ları yerel veritabanında saklayan bir uygulama gösterilmiştir.

## Mimari Yapı

Uygulama, **Clean Architecture** ve **Modüler Yapı** prensipleri ile tasarlanmıştır:

### Clean Architecture Katmanları

1. **Domain Katmanı**: 
   - Uygulamanın iş mantığını ve veri modellerini içerir
   - Repository arayüzleri ve use case sınıfları burada tanımlanır
   - Diğer katmanlardan bağımsızdır

2. **Data Katmanı**:
   - Repository implementasyonları
   - Remote ve Local veri kaynakları
   - API servisleri ve yerel veritabanı işlemleri

3. **Presentation (UI) Katmanı**:
   - Jetpack Compose UI
   - ViewModels
   - State yönetimi

### Modüler Yapı

Uygulama aşağıdaki modüllere bölünmüştür:

1. **App Modülü**:
   - Uygulama girişini ve navigasyonu içerir
   - Dependency Injection yapılandırması

2. **Core Modülleri**:
   - **core:common**: Ortak utility sınıfları ve uzantı fonksiyonları
   - **core:database**: Room veritabanı ve DAO'lar
   - **core:network**: Retrofit ve API yapılandırması
   - **core:ui**: Ortak UI bileşenleri

3. **Feature Modülleri**:
   - **feature:pokemon-list**: Pokemon listesi feature'ı
   - **feature:pokemon-detail**: Pokemon detayları feature'ı

Her feature modülü kendi içinde Clean Architecture katmanlarını (domain, data, presentation) içerir.

## Teknolojiler ve Kütüphaneler

- **Kotlin**: Programlama dili
- **Jetpack Compose**: Modern UI toolkit
- **Coroutines & Flow**: Asenkron programlama
- **Hilt**: Dependency Injection
- **Retrofit**: API çağrıları
- **Room**: Yerel veritabanı
- **Navigation Component**: Ekranlar arası geçiş
- **Coil**: Resim yükleme

## UI Özellikleri

- Material 3 tasarım dili
- Dinamik tema desteği
- Pokemon türlerine göre renk optimizasyonu
- Favori Pokemon'ları filtreleme ve sıralama
- Sayfalama (pagination) desteği

## Veri Akışı

1. UI, ViewModel aracılığıyla Use Case'leri çağırır
2. Use Case, Repository arayüzlerini kullanır
3. Repository, Remote ve Local veri kaynaklarını koordine eder
4. Veriler Flow ile UI'a akış halinde iletilir

## Geliştirici Notları

- Proje, SOLID prensiplerini takip eder
- Tüm bağımlılıklar Hilt ile yönetilir
- MVI mimarisi kullanılarak UI durumları yönetilir
- Türkçe dil desteği sağlanmıştır 