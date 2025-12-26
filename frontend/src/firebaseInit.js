import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// 환경변수에서 Firebase 설정 읽기
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || "AIzaSyDv31kIBN8GXYf3T0asUg1FAltwuw3oy9A",
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "ecommerce-80dd1.firebaseapp.com",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "ecommerce-80dd1",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "ecommerce-80dd1.firebasestorage.app",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "294677844224",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "1:294677844224:web:7c2fc575be870da44523e5"
};

console.log("Firebase Config Loaded:", {
  apiKey: firebaseConfig.apiKey ? `${firebaseConfig.apiKey.substring(0, 10)}...` : "missing",
  authDomain: firebaseConfig.authDomain,
  projectId: firebaseConfig.projectId
});

// Firebase 초기화
const firebaseApp = initializeApp(firebaseConfig);
const firebaseAuth = getAuth(firebaseApp);

export default firebaseAuth;