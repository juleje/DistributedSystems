import { h, render } from "https://esm.sh/preact@10.19.2";
import Router from "https://esm.sh/preact-router@4.1.2";
import htm from "https://esm.sh/htm@3.1.1";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.3.0/firebase-app.js";
import {
  getAuth,
  connectAuthEmulator,
  onAuthStateChanged,
} from "https://www.gstatic.com/firebasejs/10.3.0/firebase-auth.js";
import { Header } from "./header.js";
import { Trains } from "./trains.js";
import { setAuth, setIsManager } from "./state.js";
import { TrainTimes } from "./train_times.js";
import { TrainSeats } from "./train_seats.js";
import { Cart } from "./cart.js";
import { Account } from "./account.js";
import { Manager } from "./manager.js";
import { Login } from "./login.js";

const html = htm.bind(h);

let firebaseConfig;
if (location.hostname === "localhost") {
  firebaseConfig = {
    apiKey: "AIzaSyBoLKKR7OFL2ICE15Lc1-8czPtnbej0jWY",
    projectId: "demo-distributed-systems-kul",
  };
} else {
  firebaseConfig = {
    apiKey: "AIzaSyDr96EsMgB4SpIhus-kI8g-GNSiBnl652Q",
    authDomain: "distributedsystems-597e7.firebaseapp.com",
    projectId: "distributedsystems-597e7",
    storageBucket: "distributedsystems-597e7.appspot.com",
    messagingSenderId: "1017317483701",
    appId: "1:1017317483701:web:0b03b43b825b8743418afc",
    measurementId: "G-03HD6NWLGP"
  };
}

const firebaseApp = initializeApp(firebaseConfig);
const auth = getAuth(firebaseApp);
setAuth(auth);
if (location.hostname === "localhost") {
  connectAuthEmulator(auth, "http://localhost:8082", { disableWarnings: true });
}
let rendered = false;
onAuthStateChanged(auth, (user) => {
  if (user == null) {
    if (location.pathname !== "/login") {
      location.assign("/login");
      return;
    }
  } else {
    auth.currentUser.getIdTokenResult().then((idTokenResult) => {
      setIsManager(idTokenResult.claims.roles.includes("manager"));
    });
  }

  if (!rendered) {
    if (location.pathname === "/login") {
      render(html`<${Login} />`, document.body);
    } else {
      render(
        html`
            <${Header}/>
            <${Router}>
                <${Trains} path="/"/>
                <${TrainTimes} path="/trains/:trainCompany/:trainId"/>
                <${TrainSeats} path="/trains/:trainCompany/:trainId/:time"/>
                <${Cart} path="/cart"/>
                <${Account} path="/account"/>
                <${Manager} path="/manager"/>
            </${Router}>
        `,
        document.body
      );
    }
    rendered = true;
  }
});
