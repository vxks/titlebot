## Titlebot

---
#### To run:
1. `sbt clean compile docker:stage`
2. `docker-compose up`

or to create an image manually:
 - `sbt clean compile docker:publishLocal`

#### To test:
- `sbt test`

---
#### TODO:
- [ ] stream html string until the title is read - no need to read the whole doc
- [x] caching
- [ ] serve icons from backend
- [ ] add more logging
- [ ] client side URL validation

---
#### Scratch
- Save the icon to backend or fetch from frontend?
- ZIO test bc don't want mocking
- Tests for the title info fetcher - needed?
- Persistence client side so no auth
- Handle redirects - use Lihao's requests lib (vs zio-http client - no docs)

