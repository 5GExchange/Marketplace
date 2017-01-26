# Files Interface
This interface give the possibility to manage the VNF images files of NFStore.

## Upload file
This method allows to upload a file to NF Store.

The file should be inserted into HTML body as parts of a *multipart/form-data* Content.<br/>
The **Content-Type** field of the part should be configured to *application/octet-stream*.<br/>
The **Content-Disposition** field of the part should be configured setting name to file and filename with the name of file to upload.<br/>
The response header *Location* field report the URL to be used for the other request about this object.<br/>
The response body return the name of the file and the Id list of VNF Descriptors that use the files.<br/>

* **URL:** **```/NFS/files```**
* **Method:** POST
- **URL Parameters**
  - *Required Parameters:*  
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
  ```html
Content-Type: multipart/form-data;
boundary=<uuid code>
  ```
  - *Optional Parameters:*
   ```html
MD5SUM: <md5 checksum>
Provider-ID: <provider Id>
Image-Type: <image type>
  ```
- **Data Parameters**<br/>
   ```html
--uuid:<uuid code> 
Content-Type: application/octet-stream
Content-Transfer-Encoding: binary
Content-Disposition: form-data; name="file"; filename=<filename>
```
   ```html
MD5SUM: <md5 checksum>
Provider-ID: <provider Id>
Image-Type: <image type>
```
   ```html

<file data>
--uuid:<uuid code>--
```

- **Success Response:**
  - *Code:* **`201 Created`**
  - *Content:* 
   ```html
  {
  "name": <filename>,
  "vnfd_id": [<vnfd Id 1>, ..., <vnfd Id N>]
  }
```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
- **Notes:**
MD5SUM, Provider-ID and Image-Type fields can be inserted either into html header or into multipart header.<br/>
  - **MD5SUM**      : mandatory - the value will be compared with the computed value on received file<br/>
  - **Image-Type**  : optional  - if not detected the default value *unknown* will be used.<br/>
  - **Provider-ID** : optional  - if not detected the field will be left empty.<br/>


## Download file
This method allows to get from NF Store the file specified into request.<br/>

The file is returned in the HTML body as raw file or as part of a *multipart/form-data* Content; to have the response with multipart/form-data the request should contain the optional parameter *contentType=multipart*<br/>

* **URL:** **```/NFS/files```**
* **Method:** GET
- **URL Parameters**
  - *Required Parameters:*  `/<filename>`
  - *Optional Parameters:* `&contentType=multipart`
- **Header Parameters**
  - *Required Parameters:* 
  - *Optional Parameters:*
- **Data Parameters**<br/>
- **Success Response:**
  - *Code:* **`200 OK`**
  - *Header with file:* 
   ```html
Content-Type: application/octet-stream
Content-Length: <file lenght>
Content-MD5: <md5 checksum>
```
  - *Content with file:* 
   ```html
<file data>
```
  - *Header with multipart:* 
  ```html
Content-Type: multipart/form-data;
boundary=<uuid code>
  ```
  - *Content with multipart:* 
   ```html
--uuid:<uuid-code> 
Content-Type: application/octet-stream
Content-Transfer-Encoding: binary
Content-Disposition: form-data; name="file"; filename=<filename>
MD5SUM: <md5 checksum>
Provider-ID: <provider-Id>
Image-Type: <image-type>

<file data>
--uuid:<uuid-code>--
```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
  - *Code:* **`503 Service Unavailable`**
- **Sample Call:**<br/>
  curl -k -X GET http://127.0.0.1:8080/NFS/files/file1.img<br/>
  curl -k -X GET http://127.0.0.1:8080/NFS/files/file1.img&contentType=multipart
- **Notes:**
  Response 503 is returned when another operation on same file is already in progress.


## Update file
This method allows to update a file already uploaded to NF Store.<br/>

The file should be inserted into HTML body as parts of a *multipart/form-data* Content.<br/>
The **Content-Type** field of the part should be configured to *application/octet-stream*.<br/>
The **Content-Disposition** field of the part should be configured setting name to file.<br/>
The response return the name of the file and the Id list of VNF Descriptors that use the files.<br/>

* **URL:** **```/NFS/files```**
* **Method:** PUT
- **URL Parameters**
  - *Required Parameters:*  `/<filename>`
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
  ```html
Content-Type: multipart/form-data;
boundary=<uuid code>
  ```
  - *Optional Parameters:*
   ```html
MD5SUM: <md5 checksum>
Provider-ID: <provider Id>
Image-Type: <image type>
  ```
- **Data Parameters**<br/>
```html
--uuid:<uuid code> 
Content-Type: application/octet-stream
Content-Transfer-Encoding: binary
Content-Disposition: form-data; name="file"; filename=<filename>
```
```html
MD5SUM: <md5 checksum>
Provider-ID: <provider Id>
Image-Type: <image type>
```
```html

<file data>
--uuid:<uuid code>--
```
- **Success Response:**
  - *Code:* **`200 OK`**
  - *Content:* 
   ```html
  {
  "name": <filename>,
  "vnfd_id": [<vnfd Id1>, ..., <vnfd Idn>]
  }
```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X DELETE http://127.0.0.1:8080/NFS/files
- **Notes:**
  Response 503 is returned when another operation on same file is already in progress.


## Delete All files
This method allows to delete all files available into NF Store.

* **URL:** **```/NFS/files```**
* **Method:** DELETE
- **URL Parameters**
  - *Required Parameters:*  
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:*
  - *Optional Parameters:*
- **Data Parameters**
- **Success Response:**
  - *Code:* **`204 No Content`**
- **Error Response:**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X DELETE http://127.0.0.1:8080/NFS/files
- **Notes:**

## Delete file
This method allows to delete from NF Store the file specified into request.

* **URL:** **```/NFS/files```**
* **Method:** DELETE
- **URL Parameters**
  - *Required Parameters:*  `/<filename>`
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:*
  - *Optional Parameters:*
- **Data Parameters**
- **Success Response:**
  - *Code:* **`204 No Content`**
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
  - *Code:* **`503 Service Unavailable`**
- **Sample Call:**
  curl -k -X DELETE http://127.0.0.1:8080/NFS/files/file1.img
- **Notes:**
  Response 503 is returned when another operation on same file is already in progress.

