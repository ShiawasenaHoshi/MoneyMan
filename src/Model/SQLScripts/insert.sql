insert OR REPLACE into users (name, password) values ("Alisa", "59a67aa7bbd73754fc8ed966af35892d99c622f70e5a7cc9751fc897c3a63b07");
insert OR REPLACE into users (name, password) values ("Bob", "3707bc74adb363ea46b66c5aff4cfa64595dd53cfe10da18da2291d85582552e");

insert OR REPLACE into accounts (descr, user_name) values ("General", "Alisa");
insert OR REPLACE into accounts (descr, user_name) values ("2014", "Bob");

insert OR REPLACE into records (descr, amount, create_time, category_name, account_id) values ("A new car", 10000, 1433078606643, "Transport", 1);
insert OR REPLACE into records (descr, amount, create_time, category_name, account_id) values ("An old picture", 300, 1433078606643, "Other", 1);
insert OR REPLACE into records (descr, amount, create_time, category_name, account_id) values ("Unknown", 3020, 1433078606643, "NO_CATEGORY", 1);

insert OR REPLACE into records (descr, amount, create_time, category_name, account_id) values ("A cup of tea", 20, 1433078606643, "Food and Drinks", 2);
insert OR REPLACE into records (descr, amount, create_time, category_name, account_id) values ("A metro ticket", 35, 1433078606643, "Transport", 2);

insert OR REPLACE into categories (name, descr) values
("NO_CATEGORY", ""),
("Food and Drinks", "Еда и питье"),
("Cafe", "Кафе"),
("Transport", "Транспорт"),
("Health", "здоровье"),
("Other", "Другое"),
("Salary", "получка");