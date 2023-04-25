
export class TitleInfoStore {
    #key = "title_infos";

    add(iconUrl, title) {
        const prev = this.getAll();
        const toAdd = { iconUrl: iconUrl, title: title };
        localStorage.setItem(this.#key, JSON.stringify([...prev, toAdd]));
    }

    getAll() {
        return JSON.parse(localStorage.getItem(this.#key)) || [];
    }

    clear() {
        localStorage.setItem(this.#key, JSON.stringify([]));
    }
}
