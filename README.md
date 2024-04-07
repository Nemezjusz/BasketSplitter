
# BasketSplitter

The `BasketSplitter` class is responsible for splitting a list of items into delivery groups based on the available delivery options for each item.

### Usage

Create an instance of the `BasketSplitter` class, passing the absolute path to the configuration file:

```java
BasketSplitter basketSplitter = new BasketSplitter("path/to/config/file.txt");
```

Call the split method, passing a list of items:

```java
List<String> items = Arrays.asList("item1", "item2", "item3");
Map<String, List<String>> resultBasket = basketSplitter.split(items);
```
The split method returns a Map<String, List<String>> where the keys are the delivery group names and the values are the lists of items belonging to each group.

### Configuration File Format

The configuration file should be a text or json file with one line per product, in the format:
```
"product name": ["delivery option 1", "delivery option 2", ...]
```
For example:
```
"Apples": ["Express", "Standard"]
"Bread": ["Express"]
"Milk": ["Express", "Standard", "Refrigerated"]
```