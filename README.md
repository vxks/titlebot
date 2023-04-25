## Titlebot

---
#### To run:
1. `sbt clean docker:stage`
2. `docker-compose up`

#### To test:
- `sbt test`

---
#### TODO:
- [ ] stream html string until the title is read - no need to read the whole doc
- [x] caching
- [ ] serve icons from backend
- [ ] add more logging

---
#### Scratch
- Save the icon to backend or fetch from frontend?
- ZIO test bc don't want mocking
- Tests for the title info fetcher - needed?
- Persistence client side so no auth
- Handle redirects - use Lihao's requests lib (vs zio-http client - no docs)

