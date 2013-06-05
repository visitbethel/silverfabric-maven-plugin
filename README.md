[fabrician.org](http://fabrician.org/)
==========================================================================
Silver Fabric Maven Plugin
==========================================================================

Maven Plugin for creating Silver Fabric Components and Stacks.

## Help

`silverfabric:help`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:help`

#### Description:

Display help for the supported goals.

`mvn silverfabric:help -Ddetail=true -Dgoal=stacks`


## Stacks

`silverfabric:stacks`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:stacks`

#### Description:

TBD

#### Example:
```xml

```

## Grid Libararies

`silverfabric:gridlibs`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:gridlibs`

#### Description:

TBD

#### Example:
```xml

```

## Components

`silverfabric:components`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:components`

#### Description:

Perform the following actions on Components: publish, unpublish, update, delete, get info, get archives, add archives, remove archive, remove archives, assign to non cloud, get config file, update config file, delete config file, get content file path, get content file path with regexp, delete content file path with regex, add content file, get http-urls, delete http-urls, add http-urls, update http-urls, auto-detect http-urls, get patches, get script-files, add script-files, get script-files content, update script-files content, remove script-files, get script-files path with regex, delete script-files path with regex, get type names, get types, get, clean.

#### Example:
```xml
<plugin>
    <groupId>com.tibco.silverfabric</groupId>
    <artifactId>silverfabric-maven-plugin</artifactId>
    <version>0.16</version>
    <executions>
        <execution>
            <id>Create Component</id>
            <phase>install</phase>
            <goals>
                <goal>components</goal>
            </goals>
            <configuration>
            <brokerConfig>
                <brokerURL>http://localhost:8080</brokerURL>
                <username>admin</username>
                <password>admin</password>
            </brokerConfig>
                <actions>
                    <action>create</action>
                    <action>publish</action>
                </actions>
                <componentName>My Component</componentName>
                <componentType>J2EE:5.5.0.0</componentType>
                <enablerName>Tomcat 6.0 Enabler</enablerName>
                <enablerVersion>5.5</enablerVersion>
                <description>My New Component</description>
                <defaultSettings>
                    <defaultSetting>
                        <name>Default Priority</name>
                        <value>5</value>
                    </defaultSetting>
                </defaultSettings>
                <options>
                    <option>
                        <name>Maximum Activation Time in seconds</name>
                        <value>60</value>
                    </option>
                </options>
                <runtimeContextVariables>
                    <runtimeContextVariable>
                        <name>Example RCV</name>
                        <value>bar</value>
                        <type>1</type>
                        <export>true</export>
                    </runtimeContextVariable>
                </runtimeContextVariables>
                <features>
                    <feature>
                        <name>HTTP Support</name>
                        <description>Support for HTTP routing</description>
                    </feature>
                </features>
            </configuration>
        </execution>
    </executions>
</plugin>
```
