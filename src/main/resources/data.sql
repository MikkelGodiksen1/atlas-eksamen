-- Seed-data: én admin (default password 'admin123' — skift i prod)
-- BCrypt hash af 'admin123' (cost 12). Genereret via Spring Security BCryptPasswordEncoder.
INSERT INTO admin (name, email, password_hash, role) VALUES
    ('Admin', 'admin@nap.dk', '$2a$12$vBioEO47MRzoPIJdOcwYTej9C.3nw1qaSt7JXyApdX55GivBylDF6', 'admin');

-- Tre produkttyper
INSERT INTO product (name, type, base_price, description) VALUES
    ('Tekstilpose Klassisk', 'textile_bag', 12.00, 'Klassisk tote bag — 35×40 cm, hanke 50 cm.'),
    ('Rygsæk Drawstring', 'backpack', 18.00, 'Drawstring rygsæk i polyester — 40×50 cm.'),
    ('Gavepose', 'gift_bag', 9.00, 'Gavepose i polypropylen — 25×35 cm med satinhanke.');

-- Materialer (kategori = 'material')
INSERT INTO product_specification (product_id, category, spec_value, price_factor) VALUES
    (1, 'material', 'polypropylen', 1.000),
    (1, 'material', 'bomuld', 1.300),
    (1, 'material', 'genbrugsplast', 1.150),
    (2, 'material', 'polyester', 1.000),
    (2, 'material', 'bomuld', 1.350),
    (3, 'material', 'polypropylen', 1.000),
    (3, 'material', 'satin-finish', 1.250);

-- Farver (kategori = 'color')
INSERT INTO product_specification (product_id, category, spec_value, price_factor) VALUES
    (1, 'color', 'hvid', 1.000),
    (1, 'color', 'sort', 1.000),
    (1, 'color', 'navy', 1.050),
    (1, 'color', 'rød', 1.050),
    (2, 'color', 'sort', 1.000),
    (2, 'color', 'navy', 1.050),
    (3, 'color', 'hvid', 1.000),
    (3, 'color', 'guld', 1.150);

-- Størrelser (kategori = 'size')
INSERT INTO product_specification (product_id, category, spec_value, price_factor) VALUES
    (1, 'size', 'small (30×35)', 0.900),
    (1, 'size', 'medium (35×40)', 1.000),
    (1, 'size', 'large (40×45)', 1.150),
    (2, 'size', 'standard (40×50)', 1.000),
    (3, 'size', 'small (20×30)', 0.900),
    (3, 'size', 'medium (25×35)', 1.000);
