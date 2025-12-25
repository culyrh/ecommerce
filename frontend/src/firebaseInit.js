import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "REMOVED_GOOGLE_API_KEY",
  authDomain: "ecommerce-80dd1.firebaseapp.com",
  projectId: "ecommerce-80dd1",
  storageBucket: "ecommerce-80dd1.firebasestorage.app",
  messagingSenderId: "294677844224",
  appId: "1:294677844224:web:7c2fc575be870da44523e5"
};

// Firebase 초기화
const firebaseApp = initializeApp(firebaseConfig);
const firebaseAuth = getAuth(firebaseApp);

export default firebaseAuth;