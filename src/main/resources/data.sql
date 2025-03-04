INSERT INTO orders (
    id,
    customer_name,
    food_item,
    quantity,
    unit_price,
    total_price,
    address,
    restaurant_id,
    status,
    created_at
) VALUES (
             1,
             'John Doe',
             'Pizza',
             2,
             10.00,
             20.00,
             '123 Main St',
             100,
             'PENDING',
             '2023-01-01 12:00:00'
         );

INSERT INTO order_status_history (
    order_id,
    status,
    timestamp
) VALUES (
             1,
             'PENDING',
             '2023-01-01 12:00:00'
         );
