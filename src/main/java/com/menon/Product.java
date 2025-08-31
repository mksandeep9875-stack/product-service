package com.menon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "products")
@Getter
@Setter
@ToString
public class Product
{
    @Id
    String id;
    String vendorPhone; // Phone number of the vendor owner
    String name;
    String description;
    String price;
    String currency;
    String category;
    String status; // AVAILABLE, OUT_OF_STOCK, DISCONTINUED
    List<String> messages; // List of messages or comments related to the product
}
