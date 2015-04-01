Silver Fabric Schemas - Silver Lining
==========================================================================

The package for integrating maven and the TIBCO Silverfabric RESTful API. 
The schema information from TIBCO Silverfabric was obtained from the WSDLs that are 
publish. There are four WSDLs available viewable from the console under Admin->WebServices

 
`http://localhost:8000/livecluster/webservices/ApplicationAdmin?wsdl`

`http://localhost:8000/livecluster/webservices/ApplicationComponentAdmin?wsdl`

`http://localhost:8000/livecluster/webservices/EngineAdmin?wsdl`

`http://localhost:8000/livecluster/webservices/EngineDaemonAdmin?wsdl`

The schema's are published under the namespace `com.datasynapse.webservices.fabric.admin` 
we take the elements from that namespace and integrate it into a plan the plan consists of all 
the elements that could be included in a REST request. The plan XML Schema is published under the namespace `com.fedex.scm.sf`.

The Silver Fabric Schemas project bundles all the information: XSD, generated JAXB stubs and publishes it for its users. The product is the Silver Lining between maven and TIBCO Silver Fabric.


Here an example of the basic schema generated with all the optional elements. The schema can be found at [SilverLining.xsd](./SilverLining.xsd), [ApplicationAdmin.xsd](./ApplicationAdmin.xsd) and [ApplicationComponentAdmin.xsd](./ApplicationComponentAdmin.xsd).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<tns:component xmlns:tns="http://www.example.org/SilverLining" xmlns:tns1="http://admin.fabric.webservices.datasynapse.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.example.org/SilverLining SilverLining.xsd ">
  <tns:enablerName>tns:enablerName</tns:enablerName>
  <tns:enablerVersion>tns:enablerVersion</tns:enablerVersion>
  <tns:componentType>tns:componentType</tns:componentType>
  <tns:MiddlewarVersions>tns:MiddlewarVersions</tns:MiddlewarVersions>
  <tns:Department>tns:Department</tns:Department>
  <tns:Location>tns:Location</tns:Location>
  <tns:Partition>tns:Partition</tns:Partition>
  <tns:EngineBlacklisting>tns:EngineBlacklisting</tns:EngineBlacklisting>
  <tns:FailuresPerDayBeforeBlacklist>tns:FailuresPerDayBeforeBlacklist</tns:FailuresPerDayBeforeBlacklist>
  <tns:feature>
    <tns1:description>tns1:description</tns1:description>
    <tns1:infoDescription>tns1:infoDescription</tns1:infoDescription>
    <tns1:name>tns1:name</tns1:name>
    <tns1:properties>
      <tns1:name>tns1:name</tns1:name>
      <tns1:value>tns1:value</tns1:value>
    </tns1:properties>
  </tns:feature>
  <tns:runtime-variable>
    <tns1:autoIncrementType>0</tns1:autoIncrementType>
    <tns1:description>tns1:description</tns1:description>
    <tns1:export>true</tns1:export>
    <tns1:name>tns1:name</tns1:name>
    <tns1:type>0</tns1:type>
    <tns1:value xsi:type="anyType"/>
  </tns:runtime-variable>
  <tns:allocation-rule>
    <tns1:condition>
      <tns1:description>tns1:description</tns1:description>
      <tns1:properties>
        <tns1:name>tns1:name</tns1:name>
        <tns1:value>tns1:value</tns1:value>
      </tns1:properties>
      <tns1:type>tns1:type</tns1:type>
    </tns1:condition>
    <tns1:description>tns1:description</tns1:description>
    <tns1:properties>
      <tns1:name>tns1:name</tns1:name>
      <tns1:value>tns1:value</tns1:value>
    </tns1:properties>
    <tns1:ruleAction>tns1:ruleAction</tns1:ruleAction>
    <tns1:type>tns1:type</tns1:type>
  </tns:allocation-rule>
  <tns:allocation-constraint>
    <tns:currentValue>0</tns:currentValue>
    <tns:description>tns:description</tns:description>
    <tns:lastModified>0</tns:lastModified>
    <tns:maxValue>0</tns:maxValue>
    <tns:modifiedBy>tns:modifiedBy</tns:modifiedBy>
    <tns:name>tns:name</tns:name>
    <tns:type>tns:type</tns:type>
  </tns:allocation-constraint>
</tns:component>
```


