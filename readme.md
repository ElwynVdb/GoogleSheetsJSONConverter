# Google Sheets to JSON

(This project no longer works, this due to V3 being deprecated, and no longer in use by google.)

This program can be used to convert any sheets with data to a JSON

First you'll need a sheet like:

![Test Sheet](https://imgur.com/JpfNoeQ.png)

The first row holds the keys for the json, Everything below are values
Meaning the conversion will look like :
```Json
[
  {
    "Value1": "Test",
    "Value2": "Test2"
  },
  {
    "Value1": "Test1",
    "Value2": "Test3"
  }
]
```

# How to convert ?

- Go to your spreadsheet open the tab "File" and select "Publish to the web"

![Publish](https://imgur.com/NRlxJsM.png)

- Then open "Published content & settings" and click "Start publishing"

![PublishWeb](https://imgur.com/NIuBID6.png)

- Now to convert it to a json you will need to copy a part of your spreadsheet URL

Your url will roughly look like this, copy the part where "SheetID" is in this example:
```sh
https://docs.google.com/spreadsheets/d/"SheetID"/edit#gid=0
```

- Now open the jar file and put this into the text box and press "OK"

![Input](https://imgur.com/SZzv9Jr.png)

# Output

The project will automatically create the following file in the directory:
```json
"output.json"
```

# Information

This project was inspired by this image from Google Sheets API:

![Inspired](https://imgur.com/F0VRwBz.png)
 
