import { useEffect, useState } from "react";
import { TitleInfoStore } from './TitleInfoStore';

type TitlebotProps = {
  store: TitleInfoStore
}

type HistoryItemProps = {
  title: string,
  iconUrl: string
}

function Loading() {
  return <div style={Styles.loading}>
    Loading...
  </div>
}

function HistoryItem({ title, iconUrl }: HistoryItemProps) {
  return <li>
    <div style={{
      display: "flex",
      alignItems: "center",
      padding: "1px",
      borderBottom: "0.5px solid black",
    }}
    >
      <img
        src={iconUrl}
        alt="icon"
        style={{
          height: "30px",
          marginRight: "10px"
        }}
      />
      {title}
    </div>
  </li>
}

function Titlebot({ store }: TitlebotProps) {
  const [url, setUrl] = useState("");
  const [titleInfoHistory, setTitleInfoHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    console.log("Getting local history...");
    const localHistory = store.getAll().reverse();
    setTitleInfoHistory([...localHistory]);
  }, [store])

  return (
    <div style={Styles.wrapper}>
      <div style={Styles.container}>
        <div style={Styles.titleContainer}>
          <h1>Titlebot</h1>
        </div>

        <p>Enter a URL to get the page icon and title:</p>

        <div style={Styles.form}>
          {error && <div style={Styles.error}>{error}</div>}

          <input
            type="url"
            name="url"
            placeholder="https://example.com"
            pattern="https://.*"
            value={url}
            onChange={handleUrlChange}
            style={Styles.urlInput}
          />
          {isLoading ? <Loading /> : <button
            style={Styles.submitButton}
            onClick={handleSubmit}>GO</button>}
        </div>
        {titleInfoHistory.length === 0
          ? null
          : <div>
            <div style={Styles.historyAction}>
              <h4>History</h4>
              <button onClick={handleClearHistory}>Clear</button>
            </div>
            <div style={Styles.historyContainer}>
              <ul style={Styles.historyList}>
                {
                  titleInfoHistory.map(({ title, iconUrl }, idx) => {
                    return <HistoryItem key={idx} title={title} iconUrl={iconUrl} />
                  })
                }
              </ul>
            </div>
          </div>
        }
      </div>
    </div>
  );

  function pushToHistory(titleInfo) {
    setTitleInfoHistory(prev => [titleInfo, ...prev]);
    store.add(titleInfo.iconUrl, titleInfo.title);
  }

  function handleClearHistory() {
    setTitleInfoHistory([]);
    store.clear();
  }

  function handleUrlChange(e) {
    setUrl(e.target.value);
  }

  function handleSubmit() {
    setError("");
    fetchTitleInfo(url);
  }

  function fetchTitleInfo(url) {
    const data = {
      url: url
    };

    setIsLoading(true);

    fetch("http://localhost:8080/titlebot/url", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    })
      .then(resp => {
        setIsLoading(false);

        if (resp.status === 400) {
          setError("This URL is not valid");
          return Promise.reject("Invalid URL");
        }
        if (resp.status === 404) {
          setError("The URL could not be resolved, try again!");
          return Promise.reject("URL not found");
        }
        if (resp.status === 500) {
          setError("Server issue, try again later");
          return Promise.reject("Internal server error");
        }

        return resp.json();
      })
      .then(({ title, iconUrl }) => {
        pushToHistory({
          title: title,
          iconUrl: iconUrl
        });
      })
      .then(() => setUrl(""))
      .catch(e => {
        setIsLoading(false);
        console.error(e);
      })
  }
}

const Styles = {
  wrapper: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "100vh",
    width: "100vw",
    minWidth: "350px",
    backgroundColor: "rgb(243,243,243)",
    fontFamily: "Roboto, sans-serif"
  },
  container: {
    border: "3px solid lightblue",
    borderRadius: 20,
    padding: 30,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "rgb(250,250,250)",
  },
  titleContainer: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
  },
  error: {
    color: "red",
    marginBottom: "3px"
  },
  urlInput: {
    width: 200,
    height: 30,
    fontSize: "1.1em",
    border: "0.5px solid gray",
    borderRadius: 7,
    paddingLeft: 10,
    paddingRight: 10,
  },
  loading: {
    width: 120,
    height: 40,
    marginTop: 10,
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  submitButton: {
    width: 120,
    height: 40,
    marginTop: 10,
    backgroundColor: "rgb(0,100,255)",
    borderRadius: 5,
    fontWeight: "bold",
    color: "white",
    border: "none",
    cursor: "pointer"
  },
  historyContainer: {
    maxHeight: "200px",
    maxWidth: "300px",
    overflow: "scroll",
    borderTop: "1px solid rgba(0, 0, 0, 0.5)",
  },
  historyAction: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  historyList: {
    listStyle: "none",
    padding: "0px",
  }
}

function App() {
  const store = new TitleInfoStore();

  return (
    <Titlebot store={store} />
  );
}

export default App;
