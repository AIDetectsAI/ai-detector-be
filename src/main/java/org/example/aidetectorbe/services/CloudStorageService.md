# CloudStorageService - Dokumentacja

## Opis

`CloudStorageService` to interfejs służący do obsługi przesyłania obrazów do chmury. W obecnej architekturze używamy serwisu **Cloudinary**, który pozwala na darmowe, stabilne i bardzo szybkie hostowanie obrazów.

## Instrukcja krok po kroku: Jak założyć Cloudinary i pobrać klucze

Aby system przesyłania obrazków działał poprawnie na Twoim lokalnym środowisku, potrzebujesz darmowego konta na platformie Cloudinary.

### 1. Rejestracja konta (Darmowe)
- Wejdź na stronę: **https://cloudinary.com/users/register/free**
- Załóż konto (możesz użyć swojego adresu e-mail, konta Google lub GitHub).
- Darmowy plan oferuje 25 GB przestrzeni limitu co miesiąc, co jest więcej niż wystarczające dla Twojego projektu.

### 2. Pobranie danych dostępowych (Credentials)
- Zaloguj się na nowe konto.
- Przejdź do głównego panelu (Dashboard).
- Na górze ekranu (lub w sekcji Getting Started / API Keys) znajdziesz **"Product Environment Credentials"**.
- Do podłączenia aplikacji potrzebujesz trzech wartości stamtąd:
  - **Cloud Name** (np. `dxyz1abc2`)
  - **API Key** (np. `123456789012345`)
  - **API Secret** (np. `aBcDeFgHiJkLmNoPqRsTuVwXyZ`)

### 3. Konfiguracja w projekcie
- Przejdź do katalogu beackendu: `ai-detector-be/`.
- Utwórz plik o nazwie `.env` (w tym samym folderze co `pom.xml`).
- Wklej do niego dokładnie taki kawałek (podmieniając wartości na własne):
  ```env
  CLOUDINARY_CLOUD_NAME=twoja_nazwa_chmury
  CLOUDINARY_API_KEY=twoj_klucz_api
  CLOUDINARY_API_SECRET=twoj_sekret_api
  ```
*Uwaga: Plik `.env` jest celowo ignorowany przez Git (`.gitignore`). Nigdy nie udostępniaj swoich kluczy do API publicznie w serwisie GitHub!*

## Metody Interfejsu

### `uploadImage(MultipartFile file, String uniqueFileName)`

Przesyła obraz do chmury Cloudinary i zwraca gotowy bezpieczny URL (HTTPS) do pliku.

**Parametry:**
- `file` - plik obrazu z żądania HTTP (MultipartFile).
- `uniqueFileName` - unikalna nazwa wygenerowana na podstawie UUID dla obrazka (np. `550e8400-e29b-41d4.jpg`). Cloudinary pozbywa się rozszerzenia po swojej stronie używając samego UUID jako `public_id`.

**Zwraca:** `String` z linkiem URL do obrazu na serwerach Cloudinary.
**Rzuca:** `Exception`, jeśli operacja nie powiedzie się (np. zły API key, brak sieci).

## Dostępne Implementacje

### 1. CloudinaryStorageServiceImpl
- **Aktywna gdy:** `cloud.provider=cloudinary`
- **Wymagane env vars:**
  - `CLOUDINARY_CLOUD_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`
- **Zachowanie:** Inicjuje klienta za pomocą oficjalnego SDK com.cloudinary. Przesyła plik bezpośrednio do przypisanego w kodzie uniwersalnego folderu `ai-detector/` na chmurze.

## Konfiguracja w application.properties

W głównym pliku konfiguracyjnym Spring Boot (`application.properties`) połączony jest system wczytywania środowiskowego z użyciem biblioteki `dotenv-java`.

```properties
# Wybór dostawcy chmury
cloud.provider=cloudinary

# Cloudinary Configuration
cloud.cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloud.cloudinary.api-key=${CLOUDINARY_API_KEY}
cloud.cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

## Integracja z AIModelController

Gdy użytkownik wysyła plik do endpointu `/api/useModel`, kontroler automatycznie:
1. Generuje unikatową nazwę `UUID` z zachowaniem rozszerzenia.
2. Zleca przesłanie go do chmury używając `CloudStorageService` i natychmiast dostaje adres publiczny w postaci bezpiecznego URL-a.
3. Równolegle przesyła obraz do właściwej analizy w serwisie AI (Python).
4. Buduje odpowiedź i używa repozytorium do zapisania historii (szansa wg modelu, którego modelu użyto) z dołączeniem linku do obrazu z chmury do powiązanej tabeli `results` dla użytkownika.

### Przykład ostatecznej odpowiedzi z /api/useModel (JSON)

```json
{
  "certainty": 0.85,
  "modelUsed": "AIDetector-v1.0",
  "processingTimeMs": 1250,
  "imageUrl": "https://res.cloudinary.com/twoja_chmura/image/upload/v12345/ai-detector/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```
