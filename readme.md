This is the app which will call http://api.apixu.com/ api to get weather information for a given place. Here I am calling this api to get today's weather and forecast for upcoming 4 days.

Here I am using Viewmodel and live data to communicate between data layer and UI. I am using retrofit to communicate with backend to get data.

This app has only one activity which first request for User's current location and on getting location call my repository class to get weather information from backend, on getting request immidiatly I am posting a "loading" status which shows a loading UI, once I get response from api will parse the data and based on response code i either post "success" or "failed" status with data or error respectivly.
