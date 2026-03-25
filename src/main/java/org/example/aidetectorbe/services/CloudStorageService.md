# CloudStorageService - Dokumentacja

## Opis

`CloudStorageService` to interfejs służący do obsługi przesyłania obrazów do chmury. Pozwala na łatwe przełączanie między różnymi dostawcami chmury (mock, AWS S3) bez zmiany kodu aplikacji.

## Metody

### `uploadImage(MultipartFile file, String uniqueFileName)`

Przesyła obraz do chmury i zwraca URL do pliku.

**Parametry:**

- `file` - plik obrazu do przesłania (MultipartFile)
- `uniqueFileName` - unikalna nazwa pliku z rozszerzeniem (np. `550e8400-e29b-41d4.jpg`)

**Zwraca:** String z URL do przesłanego obrazu

**Rzuca:** Exception w przypadku błędu przesyłania

## Dostępne Implementacje

### 1. MockCloudStorageServiceImpl (domyślna)

- **Aktywna gdy:** `cloud.provider=mock` lub brak konfiguracji
- **Zachowanie:** Symuluje przesyłanie (500ms opóźnienie), zwraca dummy URL
- **URL Format:** `https://mock-cloud-storage.example.com/images/{uniqueFileName}`

### 2. AwsS3CloudStorageServiceImpl

- **Aktywna gdy:** `cloud.provider=aws`
- **Wymagane parametry:** `cloud.aws.s3.bucket-url`
- **Status:** Szkielet gotowy na implementację AWS SDK

## Konfiguracja w application.properties

### Podstawowa konfiguracja

```properties
# Wybór dostawcy chmury: "mock" lub "aws"
cloud.provider=mock
```

### Konfiguracja AWS S3

```properties
# Przełącz na AWS
cloud.provider=aws

# URL bucketa S3 (wymagane dla AWS)
cloud.aws.s3.bucket-url=https://twoj-bucket.s3.us-east-1.amazonaws.com
```

## Przykłady użycia

### W kontrollerze

```java
@Autowired
private CloudStorageService cloudStorageService;

public ResponseEntity<?> uploadImage(MultipartFile image) {
    // Generuj unikalną nazwę pliku
    String extension = getFileExtension(image.getOriginalFilename());
    String uniqueFileName = UUID.randomUUID().toString() + extension;

    // Prześlij do chmury
    String imageUrl = cloudStorageService.uploadImage(image, uniqueFileName);

    // Użyj URL w odpowiedzi
    response.setImageUrl(imageUrl);
    return ResponseEntity.ok(response);
}
```

## Integracja z AIModelController

Kontroler automatycznie:

1. Generuje unikalną nazwę pliku z UUID
2. Zachowuje oryginalne rozszerzenie (.jpg, .png, etc.)
3. Przesyła obraz do chmury przed przetwarzaniem AI
4. Dodaje `imageUrl` do odpowiedzi `AIModelResponse`

### Przykład odpowiedzi z /api/useModel

**Mock Provider:**

```json
{
  "certainty": 0.85,
  "modelUsed": "AIDetector-v1.0",
  "processingTimeMs": 1250,
  "imageUrl": "https://mock-cloud-storage.example.com/images/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

**AWS S3 Provider:**

```json
{
  "certainty": 0.92,
  "modelUsed": "AIDetector-v1.0",
  "processingTimeMs": 987,
  "imageUrl": "https://twoj-bucket.s3.us-east-1.amazonaws.com/a7f3c8d2-bb45-4e89-9012-3456789abcde.png"
}
```

**Struktura imageUrl:**

- **Mock:** `https://mock-cloud-storage.example.com/images/{UUID}.{extension}`
- **AWS S3:** `{bucket-url}/{UUID}.{extension}`

## Rozszerzanie

Aby dodać nowego dostawcę chmury:

1. Implementuj `CloudStorageService`
2. Dodaj `@ConditionalOnProperty` z nową wartością
3. Dodaj odpowiednie parametry do `application.properties`

Przykład:

```java
@Service
@ConditionalOnProperty(name = "cloud.provider", havingValue = "gcp")
public class GoogleCloudStorageServiceImpl implements CloudStorageService {
    // implementacja
}
```
