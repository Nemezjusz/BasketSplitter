package com.ocado.basket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
public class BasketSplitter {
    private Map<String, List<String>> deliveryOptions;

    public static void main(String[] args) {}
    public BasketSplitter(String absolutePathToConfigFile) {
        this.deliveryOptions = loadDeliveryOptions(absolutePathToConfigFile);
    }

    public Map<String, List<String>> split(List<String> items) {
        Map<String, List<String>> result = new HashMap<>();
        Map<String, List<String>> sortedDeliveryGroups = new LinkedHashMap<>();

        // Divide items into delivery groups
        for (String item : items) {
            String itemName = getItemName(item);
            List<String> availableDeliveryOptions = deliveryOptions.get(itemName);

            if (availableDeliveryOptions != null) {
                for (String deliveryOption : availableDeliveryOptions) {
                    if (!sortedDeliveryGroups.containsKey(deliveryOption)) {
                        sortedDeliveryGroups.put(deliveryOption, new ArrayList<>());
                    }
                    sortedDeliveryGroups.get(deliveryOption).add(item);
                }
            }
        }

        // Sort delivery groups by the number of items
        sortedDeliveryGroups = sortedDeliveryGroups.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Add the longest non-empty entry to the result map and remove its items from the other groups
        while (!sortedDeliveryGroups.isEmpty()) {
            Map.Entry<String, List<String>> longestEntry = sortedDeliveryGroups.entrySet().iterator().next();
            if (longestEntry.getValue().size() > 0) {
                result.put(longestEntry.getKey(), longestEntry.getValue());
                sortedDeliveryGroups.remove(longestEntry.getKey());

                for (Map.Entry<String, List<String>> entry : sortedDeliveryGroups.entrySet()) {
                    List<String> updatedList = new ArrayList<>(entry.getValue());
                    updatedList.removeAll(longestEntry.getValue());
                    entry.setValue(updatedList);
                }

                // Sort the updated sortedDeliveryGroups
                sortedDeliveryGroups = sortedDeliveryGroups.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
            } else {
                sortedDeliveryGroups.remove(longestEntry.getKey());
            }
        }
        redistributeItemsBetweenGroups(result);



        // Sort the final result map
        return result.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    private void redistributeItemsBetweenGroups(Map<String, List<String>> result) {
        List<Map.Entry<String, List<String>>> sortedGroups = result.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getValue().size(), e2.getValue().size()))
                .collect(Collectors.toList());

        // Create a copy of the original result map
        Map<String, List<String>> originalResult = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            originalResult.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        for (int i = 1; i < sortedGroups.size(); i++) {
            Map.Entry<String, List<String>> smallerGroup = sortedGroups.get(i);

            for (int j = i - 1; j >= 0; j--) {
                Map.Entry<String, List<String>> largerGroup = sortedGroups.get(j);

                List<String> itemsToRedistribute = new ArrayList<>();
                for (String item : smallerGroup.getValue()) {
                    if (deliveryOptions.get(getItemName(item)).contains(largerGroup.getKey())) {
                        itemsToRedistribute.add(item);
                    }
                }

                if (!itemsToRedistribute.isEmpty()) {
                    largerGroup.getValue().addAll(itemsToRedistribute);
                    smallerGroup.getValue().removeAll(itemsToRedistribute);

                    if (smallerGroup.getValue().isEmpty()) {
                        result.remove(smallerGroup.getKey());
                        break;
                    }
                }
            }
        }

        if (originalResult.size() == result.size()) {
            // If the size remains the same, leave the original result unchanged
            result.clear();
            result.putAll(originalResult);
        }
    }

//    private void redistributeItemsBetweenGroups(Map<String, List<String>> result) {
//        List<Map.Entry<String, List<String>>> sortedGroups = result.entrySet().stream()
//                .sorted((e1, e2) -> Integer.compare(e1.getValue().size(), e2.getValue().size()))
//                .collect(Collectors.toList());
//
//        // Create a copy of the original result map
//        Map<String, List<String>> originalResult = new HashMap<>();
//        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
//            originalResult.put(entry.getKey(), new ArrayList<>(entry.getValue()));
//        }
//        for (int i = sortedGroups.size() - 1; i >= 0; i--) {
//            Map.Entry<String, List<String>> largerGroup = sortedGroups.get(i);
//
//            for (int j = i - 1; j >= 0; j--) {
//                Map.Entry<String, List<String>> smallerGroup = sortedGroups.get(j);
//
//                List<String> itemsToRedistribute = new ArrayList<>();
//                for (String item : largerGroup.getValue()) {
//                    if (deliveryOptions.get(getItemName(item)).contains(smallerGroup.getKey())) {
//                        itemsToRedistribute.add(item);
//                    }
//                }
//
//                if (!itemsToRedistribute.isEmpty()) {
//                    largerGroup.getValue().removeAll(itemsToRedistribute);
//                    smallerGroup.getValue().addAll(itemsToRedistribute);
//
//                    if (largerGroup.getValue().isEmpty()) {
//                        result.remove(largerGroup.getKey());
//                        break;
//                    }
//                }
//            }
//        }
//        if (originalResult.size() == result.size()) {
//            // If the size remains the same, leave the original result unchanged
//            result.clear();
//            result.putAll(originalResult);
//        }
//    }

    private Map<String, List<String>> loadDeliveryOptions(String absolutePathToConfigFile) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(absolutePathToConfigFile));
            Map<String, List<String>> result = new HashMap<>();

            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String productName = parts[0].trim();
                    productName = productName.replaceAll("\"", "");
                    String deliveryOptionsString = parts[1].trim();
                    List<String> deliveryOptions = new ArrayList<>();

                    // Usuń cudzysłowy i nawiasy kwadratowe
                    deliveryOptionsString = deliveryOptionsString.replaceAll("\"", "");
                    deliveryOptionsString = deliveryOptionsString.replaceAll("\\[", "");
                    deliveryOptionsString = deliveryOptionsString.replaceAll("\\]", "");

                    // Podziel opcje dostawy na listę
                    String[] optionArray = deliveryOptionsString.split(",");
                    for (String option : optionArray) {
                        deliveryOptions.add(option.trim());
                    }

                    result.put(productName, deliveryOptions);
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load delivery options from config file: " + absolutePathToConfigFile, e);
        }
    }

    private String getItemName(String item) {
        int parenIndex = item.indexOf("(");
        return parenIndex > 0 ? item.substring(0, parenIndex).trim() : item.trim();
    }
}