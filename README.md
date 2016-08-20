# Gson Path

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.lachlanmckee/gsonpath/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.lachlanmckee/gsonpath)

An annotation processor library which generates gson type adapters at compile time which also use basic JsonPath functionality.

The benefits of this library are as follows:
- Statically generated Gson Type Adapters can remove the majority of reflection used by the Gson library.
- JsonPath syntax can reduce the number of POJOs required to parse a JSON file. An example of this is shown in the next section.
   - This allows for easier integration with other libraries which rely on a flat class structure (such as DBFlow).
- Add optional client side validation to your json using `@Nullable` and `NonNull` annotations to add mandatory field constraints.
- Generates immutable POJOs based off annotated interfaces
   - Similar to AutoValue, however you do not need to reference the concrete implementation as the Type Adapter creates it on your behalf.
   - See the [interfaces guide](guides/interfaces.md) for further details.
- Reduce the amount of repetition when creating POJOs using Path Substitutions. 
   - A more powerful version of the Gson `SerializedName` alternate key by using string replacement functionality within the `AutoGsonAdapter` annotation.
   - See the [path substitution guide](guides/path_substitution.md) for further details.

For example, given the following JSON:

```json
{
    "person": {
        "names": {
            "first": "Lachlan",
            "last": "McKee"
        }
    }
}
```

We can deserialize the content with a single class by using Gson Path. The following class demonstrates the annotations required to create a type adapter which can correctly read the content.

```java
@AutoGsonAdapter(rootField = "person.names")
public class PersonModel {
    @SerializedName("first")
    String firstName;

    @SerializedName("last")
    String lastName;
}
```

We could also write it as follows (to reduce the number of annotations required):

```java
@AutoGsonAdapter(rootField = "person.names")
public class PersonModel {
    String first;
    String last;
}
```

Both POJOs will generate the following Gson TypeAdapter:

```java
public final class PersonModel_GsonTypeAdapter extends TypeAdapter<PersonModel> {
    private final Gson mGson;

    public PersonModel_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public PersonModel read(JsonReader in) throws IOException {
        // Ensure the object is not null.
        if (!isValidValue(in)) {
            return null;
        }
        
        PersonModel result = new PersonModel();
		int jsonFieldCounter0 = 0;
        in.beginObject();
        
        while (in.hasNext()) {
			if (jsonFieldCounter0 == 1) {
				in.skipValue();
				continue;
			}
			
            switch(in.nextName()) {
                case "person":
					jsonFieldCounter0++;
					
                    // Ensure the object is not null.
                    if (!isValidValue(in)) {
                        break;
                    }
                    
					int jsonFieldCounter1 = 0;
                    in.beginObject();
                    
                    while (in.hasNext()) {
						if (jsonFieldCounter1 == 1) {
							in.skipValue();
							continue;
						}
						
                        switch(in.nextName()) {
                            case "names":
								jsonFieldCounter1++;
								
								// Ensure the object is not null.
								if (!isValidValue(in)) {
								    break;
								}
								
								int jsonFieldCounter2 = 0;
                                in.beginObject();
                                
                                while (in.hasNext()) {
									if (jsonFieldCounter2 == 2) {
										in.skipValue();
										continue;
									}
									
                                    switch(in.nextName()) {
                                        case "first":
											jsonFieldCounter2++;
											
                                            String safeValue0 = getStringSafely(in);
                                            if (safeValue0 != null) {
                                                result.firstName = safeValue0;
                                            }
                                            break;
                                            
                                        case "last":
											jsonFieldCounter2++;
											
                                            String safeValue1 = getStringSafely(in);
                                            if (safeValue1 != null) {
                                                result.lastName = safeValue1;
                                            }
                                            break;
                                            
                                        default:
                                            in.skipValue();
                                            break;
                                    }
                                }
                                
                                in.endObject();
                                break;
                                
                            default:
                                in.skipValue();
                                break;
                        }
                    }
                    
                    in.endObject();
                    break;
                    
                default:
                    in.skipValue();
                    break;
            }
        }
        
        in.endObject();
        return result;
    }

    @Override
    public void write(JsonWriter out, PersonModel value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
    
        // Begin
        out.beginObject();
    
        // Begin person
        out.name("person");
        out.beginObject();
    
        // Begin person.names
        out.name("names");
        out.beginObject();
        String obj0 = value.first;
        if (obj0 != null) {
            out.name("first");
            out.value(obj0);
        }
    
        String obj1 = value.last;
        if (obj1 != null) {
            out.name("last");
            out.value(obj1);
        }
    
        // End person.names
        out.endObject();
        // End person
        out.endObject();
        // End 
        out.endObject();
    }
}
```

## Download
This library is available on Maven, you can add it to your project using the following gradle dependencies:

```gradle
compile 'net.lachlanmckee:gsonpath:1.6.0'
apt 'net.lachlanmckee:gsonpath-compiler:1.6.0'
```
