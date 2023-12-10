# acku
   [http://acku.xream.io](http://acku.xream.io)

[![license](https://img.shields.io/github/license/x-ream/acku.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/io.xream.acku/acku.svg)](https://search.maven.org/search?q=io.xream)

    mq transaction, with tcc option


## code annotation
    @EnableAckuManagement
    @AckuProducer
    @AckuOnConsumed

## code config
    implements DtoConverter
    
## spring boot properties
    acku.app=acku-app (k8s service name)
    #acku.app=http://ip:7717 (ip:port)
    
## maven dependency
```xml
<acku.version>0.2.0</acku.version>

<dependency>
    <groupId>io.xream.acku</groupId>
    <artifactId>acku-spring-boot-starter</artifactId>
    <version>${acku.version}</version>
</dependency>

```  
