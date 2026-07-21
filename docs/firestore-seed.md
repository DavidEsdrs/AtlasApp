# Seed inicial de rotas (Firestore)

## Opcao 1: Auto-seed pelo app
- O app faz seed automaticamente quando a colecao `routes` esta vazia (primeira carga da Home).
- Isso acontece em `MainViewModel.loadRoutes()`.
- Se sua regra do Firestore bloquear escrita, use a Opcao 2.

## Opcao 2: Seed manual pelo Firebase Console
1. Abra Firestore Database no projeto Firebase do app.
2. Crie a colecao `routes`.
3. Para cada objeto em `docs/firestore-seed-routes.json`, crie um documento novo.
4. Copie os campos exatamente, incluindo `points` como array de mapas.
5. Troque `creatorId` por um UID valido da colecao `users`.

## Campos obrigatorios
- `creatorId` (String)
- `title` (String)
- `description` (String)
- `category` (String, um de: GASTRONOMIC, HISTORICAL, URBAN_ART, NATURE, ARCHITECTURE)
- `points` (Array<Map>)
- `estimatedDurationMinutes` (Number)
- `rating` (Number)
- `totalRatings` (Number)
- `createdAt` (Number)

## Observacoes de serializacao
- `category` invalida cai em fallback `HISTORICAL`.
- `points` ausente ou invalido vira lista vazia.
- O app nunca depende do campo `id` no documento; usa o id do documento Firestore.
