# Introducing... MyNews!

The ultimate social news reading platform to keep everyone informed and together!


## Project Description
MyNews is a social news following platform that aggregates articles from a variety of news sources.
Today, many people in the world feel that they are not up to date on current trends and important world events. For those that do try to keep in touch, misinformation and source reliability is a huge pain point for news readers.
Our app pulls in news from all sources, includes political bias flags, and has social gamification features with friends to promote more active news reading.


## Video/Screenshots
Download a quick video demo of how to use MyNews [here](https://github.com/saniya8/MyNews/blob/main/project/video/MyNews-Video.mp4)!

For screenshots demonstrating the use of MyNews, check out our [user guide](https://github.com/saniya8/MyNews/wiki/User-Guide)


## Getting Started

This app supports Android 8.0 or newer. The app was tested on macOS, using a Pixel 5 and Pixel 9 Pro emulator on Android 15 (API 35).

### Installing the App

Download our installer for MyNews [here](https://github.com/saniya8/MyNews/blob/main/project/releases/MyNews-Version-1-0-5.apk).
To install the app: 
1. Download the APK
2. Open an emulator device in Android Studio
3. Drag and drop the APK file onto the emulator screen
4. APK installer dialog will appear, and will automatically install the app in the emulator
5. After installation, you can find the app on your emulator

### Running Code Locally

If you would like to run the code locally: 

1. Clone the repository and run the code locally,
2. Open the project folder within your cloned directory in Android Studio
3. Ensure to add your own API keys in project/app/local.properties using the following template prior to running:

```
NEWS_API_KEY=your_news_api_key
DIFFBOT_API_KEY=your_diffbot_api_key
HUGGINGFACE_API_KEY=your_huggingface_api_key
```

You can get free API keys from the following: 
* NEWS_API_KEY: [News API](https://newsapi.org/) --> Get API Key --> Developer Plan
* DIFFBOT_API_KEY: [Diffbot API](https://docs.diffbot.com/reference/authentication) --> Get Free Token
* HUGGINGFACE_API_KEY: [Hugging Face API]() --> Access Tokens --> Create New Token --> Token Type is Read


### Additional Notes

It is important to note that MyNews currently uses free versions of APIs. This comes with limitations on usage:
- News Articles (Home Screen): these are fetched from NewsAPI. There is a limit of 100 requests per 24 hours, more specifically, 50 requests per 12 hours. If this limit is exceeded, the news articles will not be fetched. On MyNews, one request happens when: 
  - App is launched and Home Screen appears
  - Search query or filter is applied
  - Search query or filter is cleared
  - Home screen is returned to, after navigating from any other
    screen on the app
- Summary of Articles (Home Screen): each article has a Summary button. To generate the summary, the app uses the Diffbot and Hugging Face API. There is a limit of one request per second. If requests are made too quickly, the API will not be able to return the summary.

## Documentation
* [User Guide](https://github.com/saniya8/MyNews/wiki/User-Guide)
* [Design Diagrams](https://github.com/saniya8/MyNews/wiki/Design-Diagrams)
* [Project Proposal](https://github.com/saniya8/MyNews/wiki/Project-Proposal)

## Releases
* [Version 1.1.0 Release](https://github.com/saniya8/MyNews/wiki/Version-1.1.0-Release)
* [Version 1.2.0 Release](https://github.com/saniya8/MyNews/wiki/Version-1.2.0-Release)
* [Version 1.3.0 Release](https://github.com/saniya8/MyNews/wiki/Version-1.3.0-Release)
* [Version 1.4.0 Release](https://github.com/saniya8/MyNews/wiki/Version-1.4.0-Release)
* [Version 1.5.0 Release](https://github.com/saniya8/MyNews/wiki/Version-1.5.0-Release) → Final Release!


## Attributions

**News API**

This app retrieves news data from [NewsAPI](https://newsapi.org/)

**AllSides Media Bias Ratings**

This app retrieves political biases of news sources from AllSides Media Bias Ratings.

[AllSides Media Bias Ratings™](https://www.allsides.com/media-bias/media-bias-ratings) by [AllSides.com](https://www.allsides.com/unbiased-balanced-news) are licensed under a [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/). These ratings may be used for research or noncommercial purposes with attribution.

**Diffbot & Hugging Face API**

This app generates summaries of articles using [Diffbot Article API](https://docs.diffbot.com/reference/article) and [Hugging Face's BART Large CNN API](https://huggingface.co/facebook/bart-large-cnn)
