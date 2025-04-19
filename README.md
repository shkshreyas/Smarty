# Smarty - Quiz App with Jetpack Compose

Smarty is a modern quiz application built with Jetpack Compose and Firebase. It allows users to explore subjects, modules, and take quizzes.

## Features

- Hierarchical organization: Subjects > Modules > Quizzes > Questions
- Real-time data with Firebase Realtime Database
- Offline capability for faster data retrieval
- Modern UI with Jetpack Compose
- Quiz timer and scoring system
- Detailed quiz results

## Firebase Setup

1. **Create a Firebase Project**

   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" and follow the setup wizard
   - Enable Analytics if desired

2. **Add Android App to Firebase Project**

   - In the Firebase console, click the Android icon to add an app
   - Enter package name: `com.shk.smarty`
   - Download `google-services.json` and place it in the app directory

3. **Set Up Firebase Realtime Database**

   - In the Firebase console, go to "Build > Realtime Database"
   - Click "Create Database" and choose a location
   - Start in test mode (you can adjust rules later)

4. **Database Structure**
   The database should follow this structure:

   ```
   smarty-database
   |
   ├── subjects
   │   ├── subject1
   │   │   ├── id: "subject1"
   │   │   ├── title: "Mathematics"
   │   │   ├── description: "Math topics"
   │   │   ├── imageUrl: "https://example.com/math.jpg"
   │   │   └── modules: ["module1", "module2"]
   │   └── subject2
   │       └── ...
   │
   ├── modules
   │   ├── module1
   │   │   ├── id: "module1"
   │   │   ├── title: "Algebra"
   │   │   ├── description: "Algebra basics"
   │   │   ├── imageUrl: "https://example.com/algebra.jpg"
   │   │   ├── subjectId: "subject1"
   │   │   └── quizzes: ["quiz1", "quiz2"]
   │   └── module2
   │       └── ...
   │
   ├── quizzes
   │   ├── quiz1
   │   │   ├── id: "quiz1"
   │   │   ├── title: "Linear Equations"
   │   │   ├── description: "Test your skills"
   │   │   ├── moduleId: "module1"
   │   │   ├── timeInMinutes: 10
   │   │   ├── passingPercentage: 70
   │   │   └── questions: ["question1", "question2"]
   │   └── quiz2
   │       └── ...
   │
   └── questions
       ├── question1
       │   ├── id: "question1"
       │   ├── text: "What is the solution to x + 5 = 10?"
       │   ├── options: ["3", "5", "7", "9"]
       │   ├── correctOptionIndex: 1
       │   ├── explanation: "x + 5 = 10, so x = 10 - 5 = 5"
       │   └── quizId: "quiz1"
       └── question2
           └── ...
   ```

5. **Database Rules**
   For development, you can use these rules:

   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

   For production, use more restrictive rules based on your authentication setup.

## Adding Content

### Using the Firebase Console

1. Navigate to your Realtime Database
2. Click "+" to add data at the root level
3. Add each section (subjects, modules, quizzes, questions) and populate with data

### Using Firebase Admin SDK (for bulk uploads)

For bulk data uploads, you can use the Firebase Admin SDK with a script:

```javascript
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://your-project-id.firebaseio.com",
});

const db = admin.database();

// Example data
const data = {
  subjects: {
    subject1: {
      id: "subject1",
      title: "Mathematics",
      description: "Math topics",
      imageUrl: "https://example.com/math.jpg",
      modules: ["module1", "module2"],
    },
  },
  // ... (other data)
};

// Upload data
Object.keys(data).forEach((key) => {
  db.ref(key)
    .set(data[key])
    .then(() => console.log(`Data uploaded for ${key}`))
    .catch((error) => console.error(`Error uploading ${key}:`, error));
});
```

## Building and Running the App

1. Clone the repository
2. Open the project in Android Studio
3. Ensure you have the `google-services.json` file in the app directory
4. Build and run the app

## Troubleshooting

- If data isn't loading, check your internet connection and Firebase database rules
- Make sure your Firebase project is properly set up with the Realtime Database
- Check that the `google-services.json` file is in the correct location and contains the right project details
