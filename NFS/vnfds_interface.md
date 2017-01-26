# VNF Descriptor Interface
This interface give the possibility to manage VNF descriptors on NFStore.

## Add VNF Descriptor
This method allows to add one VNF descriptor to NF Store.<br/>

The descriptor should be inserted in json format into request.<br/>
The *Content-Type* field of the part should be configured to application/json.<br/>

The response body return the id of VNF descriptor inserted.<br/>

* **URL:** **```/NFS/vnfds```**
* **Method:** POST
- **URL Parameters**<br/>
  - *Required Parameters:* 
  ```html
Content-Type: application/json;
  ```
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
  - *Optional Parameters:*
- **Data Parameters**<br/>
```html
<VNF descriptor> 
```
- **Success Response:**
  - *Code:* **`201 Created`**
  - *Content:* 
   ```html
  {
  "vnfd_id": <vnfd Id>
  }
```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X POST --data @vnfd.json http://127.0.0.1:8080/NFS/vnfds/3901
- **Notes:**
The id field should not be present on VNF descriptor because the value should be inserted by NFStore.<br/>
The VNF descriptor should contain VDUs with image to be valid; NFStore add md5Sum fields of images when files are available on server.

## Get VNF Descriptor
This method allows to get from NFStore the VNF descriptor with id specified into request.<br/>
The descriptor is returned in json form into HTML body.<br/>

* **URL:** **```/NFS/vnfds```**
* **Method:** GET
- **URL Parameters**
  - *Required Parameters:*  `/<vnfd_id>`
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
  ```html
Content-Type: application/json;
  ```
  - *Optional Parameters:*
- **Data Parameters**
- **Success Response:**
  - *Code:* **`200 OK`**
  - *Content:* 
   ```html
  <VNF descriptor> 
  ```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X GET http://127.0.0.1:8080/NFS/vnfds/3901
- **Notes:**

## Get VNF Descriptors
This method allows to retrieve all VNF descriptors available into NF Store.<br/>

* **URL:** **```/NFS/vnfds```**
* **Method:** GET
- **URL Parameters**
  - *Required Parameters:*  
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
  ```html
Content-Type: application/json;
  ```
  - *Optional Parameters:*
- **Data Parameters**
- **Success Response:**
  - *Code:* **`200 OK`**
  - *Content:* 
   ```html
   {
   "vnfds":[
   		<VNF descriptor 1>,
   		...,
   		<VNF descriptor N>,
   ]}
  ```
- **Error Response:**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X GET http://127.0.0.1:8080/NFS/vnfds
- **Notes:**

## Modify VNF Descriptor
This method allows to modify one VNF descriptor already available into NF Store.<br/>
The new descriptor should be inserted in json format into request.<br/>

The response return the id of VNF descriptor modified.

* **URL:** **```/NFS/vnfds```**
* **Method:** PUT
- **URL Parameters**
  - *Required Parameters:*  `/<vnfd_id>`
  - *Optional Parameters:*
- **Header Parameters**
  - *Required Parameters:* 
   ```html
Content-Type: application/json;
   ```
  - *Optional Parameters:*
- **Data Parameters**<br/>
```html
<VNF descriptor> 
```
- **Success Response:**
  - *Code:* **`200 OK`**
  - *Content:* 
   ```html
  {
  "vnfd_id": <vnfd Id>
  }
   ```
- **Error Response:**
  - *Code:* **`400 Bad Request`**
  - *Code:* **`404 Not Found `**
  - *Code:* **`500 Internal Server Error`**
- **Sample Call:**
  curl -k -X PUT --data @vnfd2.json http://127.0.0.1:8080/NFS/vnfds/3901
- **Notes:**<br/>
The id field of VNF descriptor should be present and should be the same specified into request URL.

## Delete VNF Descriptor
This method allows to delete from NF Store the VNF Descriptor specified into request and all files specified into Vdus fields.<br/>
The file used also by other VNF descriptors will not be removed.<br/>

* **URL:** **```/NFS/vnfds```**
* **Method:** DELETE
- **URL Parameters**
  - *Required Parameters:*  `/<vnfd_id>`
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
- **Sample Call:**
  curl -k -X DELETE http://127.0.0.1:8080/NFS/vnfds/1309
- **Notes:**

## Delete All VNF Descriptors
This method allows to delete all VNF Descriptors from NF Store with all files specified into Vdus fields.

* **URL:** **```/NFS/vnfds```**
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
  curl -k -X DELETE http://127.0.0.1:8080/NFS/vnfds
- **Notes:**
