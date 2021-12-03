### Note-App

## Architecture
__MVVM__ - MVVM stands for Model, View, ViewModel. MVVM is one of the architectural patterns which enhances separation of concerns, it allows separating the user interface logic from the business (or the back-end) logic.

## Android Jetpack components:-
1. __Navigation Components__ - Navigation component helps you implement navigation, from simple button clicks to more complex patterns, such as app bars and the navigation drawer. The Navigation component also ensures a consistent and predictable user experience by adhering to an established set of principles.

2. __Android Room Persistence__ - It is a SQLite object mapping library. Use it to Avoid boilerplate code and easily convert SQLite table data to Java objects. Room provides compile time checks of SQLite statements and can return RxJava, Flowable and LiveData observables.

3. __Kotlin Coroutines__ - A coroutine is a concurrency design pattern that you can use on Android to simplify code that executes asynchronously. On Android, coroutines help to manage long-running tasks that might otherwise block the main thread and cause your app to become unresponsive.

4. __ViewModel__ - It manages UI-related data in a lifecycle-conscious way. It stores UI-related data that isn't destroyed on app rotations.

5. __LiveData__ - It notifies views of any database changes. Use LiveData to build data objects that notify views when the underlying database changes.

6. __Kotlin__ - Kotlin is a modern statically typed programming language used by over 60% of professional Android developers that helps boost productivity, developer satisfaction, and code safety.

          It also uses RecyclerView with DiffUtill to improves overall app performances
## Features:-
1. Save Note In a Local db
2. Update
3. Swipe To Delete
4. Search
5. Color Picker (Colorful notes)
5. RecyclerView Animations
6. Visibility Animation

## Libraries Used:-
   <h4>Library used</h4>
<ul>
<li><a href="https://developer.android.com/topic/libraries/architecture/room" target="_blank">Room</a></li>
<li><a href="https://developer.android.com/topic/libraries/architecture/viewmodel" target="_blank">Viewmodel</a></li>
<li><a href="https://developer.android.com/topic/libraries/architecture/livedata">Livedata</a></li>
<li><a href="https://developer.android.com/kotlin/coroutines" target="_blank">Coroutines</a></li>
<li><a href="https://material.io/develop/android/docs/getting-started/" target="_blank">Material library</a></li>
<li><a href="https://developer.android.com/guide/navigation/navigation-getting-started" target="_blank">Navigation Component</a></li>
   
</ul>

## Samed-Sacid-Soyalan
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# NoteApp

### Deadline

We'll be waiting for your solution within 4 days.

### Goal ###

Develop a simple note app that allows the user to save/edit/delete any kind of note and display them in a list.

### Functional Requirements ###

* Kotlin is preferred but not a must.
* Users must be able to create notes with input fields such as title, description, image url (input can be optional) and store it locally on their phones.
* Created note must contain a created date.
* There must be a way to display all saved notes in the list. An item on the list must contain the created date (dd/mm/yyyy), the image if url is available, title and max. 2 lines of description.
* There must be a way to edit/delete previously created notes. But edited notes must contain an (edited) tag somewhere while being displayed on the list.
* All data should be persisted locally.

### UI Suggestions ###

It doesn't need to be super pretty, but it shouldn't be broken as well. The design is mostly up to you as long as creating, listing and editing/deleting features are available to use.

Nice to have:
* Animations/Transitions
* At least one custom view

### Expectations ###

Consider this as a showcase of your skills.
Approach it as if you are going to make a pull request on our main/master branch.

We are expecting at least:
* Clear, defined architecture.
* Apply the Material Design Guidelines as much as possible.
* Meaningful tests (You do not need to have 100% coverage, but we will be looking for tests).
* Good and lint verified syntax.
* We expect a clear history in the repo. We don't mind your choice of git strategy as long as it has a track of your progress.
* The repo must be private and should not contain any references to Getir in it.
* The code must compile.
* The code must be production ready. Unit tests are expected.
