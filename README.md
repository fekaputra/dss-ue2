# DSS UE2 Summer term 2020
Submission by Jacob Palecek (01526624)

## About this program
This tool can be used to perform analyses on excel sheets to explain how values are created
It does so by parsing an excel sheet into an RDF ontology. As soon as this is done certain queries can be run against the excel file

## Building
Assuming all the dependencies are installed the program can simply be compiled and run via maven.
The preferred way is to do so over docker however. from the root of the project simply run this command to build:
`docker build -t dss/ue2 .`
then you can run the image like so:
`docker run -d -p 8080:8080 dss/ue2`
Note that the database is packages into the program itself, it uses Apache TDB2, thus all data will be persisted within the container

## Usage
The program provides a number of HTTP endpoints for interaction. Note that they are all based on POST
requests, since that was faster to develop even though this could be done in a more standard way. If you feel like it
feel free to fork the project and add that functionality.
ATTENTION: The program may perform rather poorly. This is due to the unoptimized implementation which is owed to the fact that I still 
wouldn't consider myself good with RDF, SPARQL and the related databases.

So don't fret if some requests take a few seconds, particularly for complex analyses

###Upload a new excel file
To upload a new excel file send it as multipart-formdata to the `/uploadFile` endpoint.
Note that the form field with the file is also supposed to be named "file".
For your convenience here is a curl command you can try, just substitute the file for your own excel sheet.

```bash
curl --location --request POST 'http://localhost:8080/uploadFile' \
--form 'file=@/Users/jacob/Documents/GitProjects/dss20-ue2/backend/src/main/resources/xlsx/simple.xlsx'
```
The server will then respond with some information about the added workbook, notably the hash value of the excel-file.
This is incredibly important as we will use this to refer to your uploaded file.

###List uploaded workbooks
You can list all uploaded workbooks by simply sending a POST request to `/getAllWorkbooks`.
```
curl --location --request POST 'http://localhost:8080/getAllWorkbooks'
```

The server will in response list all available workbooks. e.g.:
```json
[
    {
        "filename": "Simple.xlsx",
        "filehash": "F55024C721C9045D5145718D0268734A99B06EFE",
        "worksheets": null
    },
    {
        "filename": "EdgeCase.xlsx",
        "filehash": "3C1DE3C4E68B20A873AA7F8A2792EA455D47B3AB",
        "worksheets": null
    },
    {
        "filename": "Bodenabtrag.xlsx",
        "filehash": "FA6DCA67F2F859C726654D78A73898A5BFB52FD0",
        "worksheets": null
    }
]
```
Note that "worksheets" is always null so far. This is because it is a featured intended
to make frontend designs more user-friendly and hasn't been implemented yet.

###Get information about a cell
At this point we can get information about a cell, notably which other cells it depends on for creating it's value
To do so send a POST request to `/getCell` with a request body of the following format:
```aidl
{
	"workbookHash": "F55024C721C9045D5145718D0268734A99B06EFE",
	"row": 1,
	"column": 3,
	"worksheet": "Sheet1",
	"resolveTransitiveDependencies": true
}
```
What is important here is that the hash corresponds to the hash value of a previously uploaded file. You can find it by requesting all uploaded workbooks as was described earlier
The "worksheet" must refer to the specific name of the Sheet from the excel file
Row and column are only based on numbers and are represented as a 0 based index.
i.e. row=1 and column=3 here refers to the cell D2.

When sending that request you will get information about the cell such as the value, formula and what cells it depends on in case
it uses a formula. Note that you can choose whether or not to resolve transitive dependencies. This is where RDF really starts to shine.
Assume we have a worksheet with values in the range A0:A10. And these values are added with values in B0:B10 and the results are put into C0C:10.
If we now have a cell with formula `SUM(C0:C10)` if we do not resolve transitive dependencies we will get
C0:C10 as dependent cells. If we DO resolve transitive dependencies we get A0:C10 as a result.
What you want will depend on what kind of analysis you intend to perform.

Note that the dependent cells will not have their respective dependencies resolved. This is done for performance reasons on larger sheets, since a simple transitive sum might result in the whole sheet being returned.

Here is again an exmaple via curl:
```aidl
curl --location --request POST 'http://localhost:8080/getCell' \
--header 'Content-Type: application/json' \
--data-raw '{
	"workbookHash": "F55024C721C9045D5145718D0268734A99B06EFE",
	"row": 1,
	"column": 3,
	"worksheet": "Sheet1",
	"resolveTransitiveDependencies": true
}'
```
###Validate assertions
To validate an assertion simply send a POST request to `/checkAssertion`
The request body has to be in a very specific format, which is again best explained using an example:
```
{
	"rowStart": 1,
	"rowEnd": 17,
	"columnStart": 0,
	"columnEnd": 2,
	"worksheet": "Sheet1",
	"workbookHash": "F55024C721C9045D5145718D0268734A99B06EFE",
	"operator": "gte",
	"numVal": 0
}
```
With rowStart, rowEnd, columnStart, and columnEnd we define a cellRange we want to check the assertion on. Once again 0 based indices are used.
The sheet and workbook hash work as they do when requesting a single cell.
Into numVal you will enter the numeric value you want to check against and into `operator` the operator you wan to use.
The operator has to be one of a few specific string values: "eq", "gt", "gte", "lt", "lte", where
 * "eq" = "="
 * "gt" = ">"
 * "gte" = ">="
 * "lt" = "<"
 * "lte" = "<="
 
 In the example given we assert that for all cells in `A2:C18` the value is `>= 0` i.e. greater or equal to 0
The server will then return the cells that DO NOT fulfill the assertion's requirement. i.e. all returned cells violate the assertion.
An empty result thus means the assertion is correct.

Please note that the resulting cells do not have their dependencies resolved. This is done for performance reasons on larger sheets.
To find out details about the cells use the previously described endpoint for inspecting specific cells

Here is an example request via curl:
```aidl
curl --location --request POST 'http://localhost:8080/checkAssertion' \
--header 'Content-Type: application/json' \
--data-raw '{
	"rowStart": 1,
	"rowEnd": 17,
	"columnStart": 0,
	"columnEnd": 2,
	"worksheet": "Sheet1",
	"workbookHash": "F55024C721C9045D5145718D0268734A99B06EFE",
	"operator": "gte",
	"numVal": 0
}'
```
##License and contibuting
This small project should just be seen as a demo of what RDF and OWL can provide. Feel free to use it as you please as long as it isn't commercial and properly attributed (see license in github)

