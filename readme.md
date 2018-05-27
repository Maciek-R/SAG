1. Aktor główny tworzy podaktorów.
2. Aktor główny przesyła informację do podaktorów o tym, aby rozpoczęli proces pobierania opisów produktów.
3. Każdy z podaktorów:
3.1 Pobiera listę wszystkich linków do kategorii produktów.
3.2 Pobiera listę wszystkich linków do produktów na podstawie linków do kategorii.
3.3 Powstaje Mapa[linkDoKategorii -> ListaLinków do produktów]
3.4 Pobiera losowe opisy produktów
3.5 Pobrane opisy produktów są wysyłane do aktora głównego
4. Aktor główny zbiera informacje o wszystkich produktach.