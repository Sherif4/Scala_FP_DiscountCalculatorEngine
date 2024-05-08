# **Discount Checker and calculator using Scala Functional Programming**

- This script was programmed to iterate on every transaction after parsing it from CSV file.
- Check if the transaction qualifies for any of the discount rules.
- Calculate the discount for each rule.
- If the transaction qualifies for more than 1 rule, discount would be the average between the highest 2 rules the transaction qualifies for.

## **Rules that qualify transactions for discounts:**
1. less than 30 days remaining for the product to expire (from the day of transaction) -> 1-29% discount.
2. Cheese and wine products are on sale -> 5-10% discount.
3. Products that are sold on 23rd of March have a special discount! (Celebrating the end of java project?) -> a whooping 50% discount xD.
4. bought more than 5 of the same product -> 5-10% discount.
5. Sales that are made through the App -> 5% discount or more.
6. Sales that are made using Visa cards -> 5% discount.  

- After checking for discounts and calculate them for each transaction, the processed data is inserted into a postgres database.
- every transaction we iterate on in this script is logged into a log file for information or warnings.