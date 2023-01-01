# s3manager

File Explorer to manage servers compatible with S3 Object Storage Protocol



Current feature list

* Audio and Video playback (opus, ogg, oga, mp3, m4a, flac, mka, mkv, mp4, m4v, webm)
* Picture preview (jpg, jpeg, png, gif, webp)
* Plain text file preview (txt, md)
* Webpage viewer (htm, html)
* Multiple account support
* Open object in external web browser
* Create buckets
* Delete buckets
* Delete files
* Delete folders
* File upload
* File download
* pdf file reader using user provided pdf.js server
* file sharing links
* get object info
* get bucket info
* Set CORS policy

Planned feature list

* Nothing for now

This app is a work in progress, so it have some bugs that need to be fixed

You need to setup a pdf.js server to use pdf viewer. Just download latest version from official website and upload to any web server with ssl on same root domain than S3 server (can be same subdomain or a different one). Then set url to pdfjs root folder like https://example.com/pdfjs-dist

[<img src="app-store-badges/fdroid.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/asgardius.page.s3manager/)

[<img src="app-store-badges/play-store.png"
    alt="Get it on Google Play"
    height="80">](https://play.google.com/store/apps/details?id=asgardius.page.s3manager)

F-droid release may take a few days to get updated [More info here](https://www.f-droid.org/en/docs/FAQ_-_App_Developers/#ive-published-a-new-release-why-is-it-not-in-the-repository)

[Steps to joining to Google Play Alpha testing channel are available here](https://forum.asgardius.company/d/1-asgardius-s3-manager-testing)

You can get help at https://forum.asgardius.company/t/s3-manager

Supported languages

* English
* Spanish

Knnown issues

* Object listing can be slow on buckets with a lot of objects (4000+)
* Slow user interface on some low-end devices
* Running screen restarts after toggling system dark mode

Known supported providers

* Amazon Web Services
* Scaleway Elements
* Oracle Cloud (partial)
* Wasabi Cloud
* MinIO

Known not supported providers

* Google Cloud