DELETE FROM RESERVA_SILLA;
DELETE FROM BOLETO_SILLA;
DELETE FROM COMPRA_PRODUCTO;
DELETE FROM PAGO;
DELETE FROM BOLETO;
DELETE FROM RESERVA;
DELETE FROM COMPRA;
DELETE FROM SILLA;
DELETE FROM FUNCION;
DELETE FROM SALA;
DELETE FROM PELICULA;
DELETE FROM PRODUCTO;
DELETE FROM TARIFA;
DELETE FROM SEDE;
DELETE FROM SIGMA_CARD;
DELETE FROM CLIENTE;
DELETE FROM ADMIN;
DELETE FROM USUARIO;

INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES(1, 'admin@sigma.com', 'admin_pass','ADMIN');
INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES(2, 'cliente1@correo.com', 'cliente1_pass', 'CLIENTE');
INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES(3, 'cliente2@correo.com', 'cliente2_pass', 'CLIENTE');

INSERT INTO ADMIN (ID, NOMBRE) VALUES(1, 'Juan Perez');

INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES(2, 'Ana Gomez', '2025-09-15');
INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES(3, 'Carlos Rodriguez', '2025-09-15');

-- Sedes (múltiples por ciudad)
INSERT INTO SEDE (ID, NOMBRE, CIUDAD) VALUES (1, 'Salitre Plaza',     'Bogotá');
INSERT INTO SEDE (ID, NOMBRE, CIUDAD) VALUES (2, 'Gran Estación',     'Bogotá');
INSERT INTO SEDE (ID, NOMBRE, CIUDAD) VALUES (3, 'Parque La Colina',  'Bogotá');
INSERT INTO SEDE (ID, NOMBRE, CIUDAD) VALUES (4, 'Viva Envigado',     'Medellín');
INSERT INTO SEDE (ID, NOMBRE, CIUDAD) VALUES (5, 'El Tesoro',         'Medellín');

-- Salas por sede
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(1,  1, 150, '2D', 1);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(2,  2, 100, '3D', 1);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(3,  3,  80, 'VIP',1);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(4,  1, 140, '2D', 2);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(5,  2, 110, '3D', 2);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(6,  1, 160, '2D', 3);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(7,  2,  90, 'VIP',3);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(8,  1, 170, '2D', 4);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(9,  2, 105, '3D', 4);
INSERT INTO SALA (ID, NUMERO_SALA, CAPACIDAD, TIPO, SEDE_ID) VALUES(10, 1, 120, '2D', 5);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(1, 'Dune: Part Two', 'Ciencia ficción', 'PG-13', 166, 'Denis Villeneuve', 'Timothée Chalamet, Zendaya',
'/videos/dune_trailer.mp4',
'Sigue el viaje mítico de Paul Atreides mientras se une a Chani y los Fremen en una guerra de venganza contra los conspiradores que destruyeron a su familia.',
'En Cartelera', '/Images/Posters/dune_poster.png', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(2, 'Joker', 'Thriller psicológico', 'R', 122, 'Todd Phillips', 'Joaquin Phoenix, Robert De Niro',
'/videos/joker_trailer.mp4',
'Un comediante fracasado desciende a la locura y se convierte en una figura icónica del crimen.',
'En Cartelera', '/Images/Posters/joker_poster.png', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(3, 'Godzilla x Kong: The New Empire', 'Acción, Ciencia ficción', 'PG-13', 115, 'Adam Wingard', 'Rebecca Hall, Brian Tyree Henry',
'/videos/godzillavskong_trailer.mp4',
'Kong y Godzilla se unen para luchar contra una amenaza colosal desconocida que se esconde en nuestro mundo.',
'En Cartelera', '/Images/Posters/godzillavskong_poster.png', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(4, 'Los Pitufos', 'Animación, Comedia, Aventura', 'PG', 90, 'Raja Gosnell', 'Hank Azaria, Neil Patrick Harris, Jayma Mays',
'/videos/pitufos_trailer.mp4',
'Un grupo de pequeños seres azules de tres manzanas de altura huyen de su pueblo natal después de que el malvado hechicero Gargamel los descubre, y terminan en el Central Park de Nueva York.',
'En Cartelera', '/Images/Posters/LosPitufos.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(5, 'Barbie', 'Comedia, Fantasía, Romance', 'PG-13', 114, 'Greta Gerwig', 'Margot Robbie, Ryan Gosling',
'/videos/BARBIE Tráiler Español Latino (2023) - ONE Media Español (1080p, h264).mp4',
'Barbie vive en Barbieland donde todo es perfecto. Sin embargo, cuando tiene pensamientos sobre la muerte, decide aventurarse en el mundo real.',
'En Cartelera', '/Images/Posters/Barbie.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(6, 'Frozen 2', 'Animación, Aventura, Fantasía', 'PG', 103, 'Chris Buck, Jennifer Lee', 'Kristen Bell, Idina Menzel, Josh Gad',
'/videos/Frozen 2 de Disney  Nuevo Tráiler Oficial en español  HD - Disney España (1080p, h264).mp4',
'Elsa escucha una misteriosa voz llamándola. Junto a Anna, Kristoff, Olaf y Sven, emprende un viaje épico para descubrir el origen de sus poderes y salvar su reino.',
'En Cartelera', '/Images/Posters/Frozen2.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(7, 'Interstellar', 'Ciencia ficción, Drama, Aventura', 'PG-13', 169, 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain',
'/videos/Interstellar - Tráiler final en español HD - Warner Bros. Pictures España (1080p, h264).mp4',
'Un grupo de exploradores viaja a través de un agujero de gusano en el espacio en un intento de asegurar la supervivencia de la humanidad.',
'En Cartelera', '/Images/Posters/Interestellar.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(8, 'Las Aventuras de Sharkboy y Lavagirl', 'Aventura, Familia, Ciencia ficción', 'PG', 93, 'Robert Rodriguez', 'Taylor Lautner, Taylor Dooley, Cayden Boyd',
'/videos/THEADV~1.MP4',
'Un niño solitario inventa dos superhéroes imaginarios: Sharkboy y Lavagirl. Cuando estos personajes cobran vida, lo llevan al Planeta Drool donde debe salvar su mundo de ensueño.',
'En Cartelera', '/Images/Posters/Las aventuras de sharkboy y lavagirl.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(9, 'No Se Aceptan Devoluciones', 'Comedia, Drama', 'PG-13', 122, 'Eugenio Derbez', 'Eugenio Derbez, Karla Souza, Jessica Lindsey',
'/videos/NO SE ACEPTAN DEVOLUCIONES - Tráiler Oficial España - Estreno en cines el 30 de abril - Filmax (1080p, h264).mp4',
'Un playboy empedernido ve su vida cambiar cuando una mujer del pasado le deja una bebé diciendo que es su hija. Cuando aprende a amarla, la madre reaparece para reclamarla.',
'En Cartelera', '/Images/Posters/NoSeAceptanDevoluciones.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(10, 'Spider-Man: Brand New Day', 'Acción, Aventura, Superhéroes', 'PG-13', 135, 'TBA', 'Tom Holland, Zendaya',
'/videos/SPIDER-MAN BRAND NEW DAY – "Shadows of the Symbiote" Trailer (Concept Version) - Teaser Universe (1080p, h264).mp4',
'Spider-Man enfrenta nuevas amenazas mientras lidia con las sombras del simbionte y descubre secretos que cambiarán su vida para siempre.',
'En Cartelera', '/Images/Posters/SpiderManBrandNewDay.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(11, 'Star Wars: The Force Awakens', 'Ciencia ficción, Aventura, Acción', 'PG-13', 138, 'J.J. Abrams', 'Daisy Ridley, John Boyega, Harrison Ford',
'/videos/Star Wars The Force Awakens Trailer (Official) - Star Wars (1080p, h264).mp4',
'Tres décadas después de la derrota del Imperio Galáctico, una nueva amenaza surge. Una joven chatarrera y un stormtrooper desertor se unen en una búsqueda desesperada del último Jedi.',
'En Cartelera', '/Images/Posters/starWarsTheForceAwakens.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(12, 'Teen Beach Movie', 'Musical, Comedia, Romance', 'G', 110, 'Jeffrey Hornaday', 'Ross Lynch, Maia Mitchell, Grace Phipps',
'/videos/TEEN BEACH MOVIE (Trailer español) - Cine Fantasia (720p, h264).mp4',
'Dos adolescentes se transportan mágicamente dentro de una película musical de playa de los años 60 donde deben encontrar la forma de regresar a casa.',
'En Cartelera', '/Images/Posters/teenbeachmovie.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(13, 'Violet Evergarden: Eternity and the Auto Memory Doll', 'Animación, Drama, Fantasía', 'PG-13', 90, 'Haruka Fujita', 'Yui Ishikawa, Minako Kotobuki',
'/videos/Violet Evergarden – Eternity and the Auto Memory Doll   In Cinemas December 5 - Crunchyroll Store Australia (1080p, h264).mp4',
'Violet Evergarden ayuda a una joven noble a prepararse para su debut en sociedad, mientras ambas descubren el verdadero significado del amor y la familia.',
'En Cartelera', '/Images/Posters/VioletEvergardenAutoMemoryDoll.jpg', TRUE);

INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) VALUES
(14, 'WALL-E', 'Animación, Ciencia ficción, Romance', 'G', 98, 'Andrew Stanton', 'Ben Burtt, Elissa Knight, Jeff Garlin',
'/videos/WALL•E  Tráiler Oficial  Disney · Pixar Oficial - Disney España (240p, h264).mp4',
'Un pequeño robot recolector de basura llamado WALL-E se embarca en una aventura espacial que decidirá el destino de la humanidad.',
'En Cartelera', '/Images/Posters/Wall-E.jpg', TRUE);


INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(1, 'Entrada General 2D', 15.00, '2025-01-01');
INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(2, 'Entrada General 3D', 18.00, '2025-01-01');
INSERT INTO TARIFA (ID, NOMBRE, PRECIO_BASE, VIGENCIA) VALUES(3, 'Entrada VIP',       25.00, '2025-01-01');

INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(4, 'Snack pequeño', 'Snack pequeño para compra rápida', '/Images/Menu/Snack_pequeño.png', 'Original', 'CONFITERIA', 3.00, 'Disponible', TRUE);

INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(2, 'Perro caliente',       'Hot Dog Clásico con papas', '/Images/Menu/Perro_caliente.png', 'Original', 'COMIDA',  8.75, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(5, 'Crispetas',     'Palomitas de maíz clásicas',       '/Images/Menu/Crispetas.png', 'Dulce,Salada,Mixta', 'COMIDA',  7.50, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(6, 'Gaseosa',       'Refresco pequeño (330ml)',         '/Images/Menu/Gaseosas.png', 'CocaCola,Manzana,Naranja', 'BEBIDA',  4.00, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(7, 'ICEE',          'Bebida ICEE (pequeña)',            '/Images/Menu/ICEE.png', 'Fresa,Mora_Azul,Limón', 'BEBIDA',  5.00, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(8, 'Chocolatas',    'Selección de chocolates y dulces',  '/Images/Menu/Chocolatas.png', 'Snickers,Jet,Jumbo,Hersheys', 'CONFITERIA', 6.50, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(9, 'Pipsas',        'Porción de pizza estilo cine',     '/Images/Menu/Pipsas.png', 'Queso,Peperoni,Mixta', 'COMIDA',  11.00, 'Disponible', TRUE);

-- COMBOS
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(10, 'Shake Up Your Snacking', 'Tus palomitas, tus reglas', '/Images/Combos/Combo_shake_up_your_shaking.png', 'Original', 'COMBO', 14.99, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(11, 'Combo Nachos', 'Nachos con queso y hot dog clásico', '/Images/Combos/Combo_nachos.png', 'Original', 'COMBO', 12.50, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(12, 'Snack & Sip', 'Crispetas y bebida ilimitada', '/Images/Combos/Combo_snack_sip.png', 'Original', 'COMBO', 13.75, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(13, 'Combo Pitufos', 'Crispetas, vaso con pitufos y tu peluche favorito', '/Images/Combos/Combo_pitufos.png', 'Original', 'COMBO', 11.99, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(14, 'Gaseosas Exclusivas', 'Disfruta la mejor selección de bebidas', '/Images/Combos/Combo_gaseosas_exclusivas.png', 'CocaCola,Fanta,MrBig,Manzana,Limón', 'COMBO', 8.99, 'Disponible', TRUE);
INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES(15, 'Combo Crispetas y Perro', 'Crispetas grandes, M&M, ICEE, gaseosa y hot dog clásico', '/Images/Combos/Combo_crispetas_perro.png', 'Original', 'COMBO', 15.50, 'Disponible', TRUE);

-- Funciones en distintas ciudades y sedes
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(1,  '2025-09-15', '12:50', 'Activa', TIME '02:46:00', TRUE, 1, 1); -- Bogotá / Salitre Plaza
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(2,  '2025-09-15', '15:45', 'Activa', TIME '02:46:00', TRUE, 1, 1);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(3,  '2025-09-15', '18:45', 'Activa', TIME '02:46:00', TRUE, 1, 2);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(4,  '2025-09-15', '21:45', 'Activa', TIME '02:46:00', TRUE, 1, 2);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(5,  '2025-09-15', '13:10', 'Activa', TIME '02:46:00', TRUE, 1, 4); -- Bogotá / Gran Estación
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(6,  '2025-09-15', '19:40', 'Activa', TIME '02:46:00', TRUE, 1, 5);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(7,  '2025-09-15', '22:40', 'Activa', TIME '02:46:00', TRUE, 1, 5);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(8,  '2025-09-15', '11:00', 'Activa', TIME '02:46:00', TRUE, 1, 6); -- Bogotá / Parque La Colina
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(9,  '2025-09-15', '11:40', 'Activa', TIME '02:46:00', TRUE, 1, 6);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(10, '2025-09-15', '12:00', 'Activa', TIME '02:46:00', TRUE, 1, 6);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(11, '2025-09-15', '13:55', 'Activa', TIME '02:46:00', TRUE, 1, 7);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(12, '2025-09-15', '15:30', 'Activa', TIME '02:46:00', TRUE, 1, 7);

-- Medellín sedes
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(13, '2025-09-15', '16:10', 'Activa', TIME '02:46:00', TRUE, 1, 8); -- Viva Envigado
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(14, '2025-09-15', '19:10', 'Activa', TIME '02:46:00', TRUE, 1, 9);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(15, '2025-09-15', '21:30', 'Activa', TIME '02:46:00', TRUE, 1, 9);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(16, '2025-09-15', '20:30', 'Activa', TIME '02:02:00', TRUE, 2, 10); -- El Tesoro

-- Funciones para Los Pitufos (película 4) - 2025-11-11
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(200, '2025-11-11', '10:00', 'Activa', TIME '01:30:00', TRUE, 4, 1); -- Bogotá / Salitre Plaza
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(201, '2025-11-11', '14:00', 'Activa', TIME '01:30:00', TRUE, 4, 2);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(202, '2025-11-11', '16:30', 'Activa', TIME '01:30:00', TRUE, 4, 3);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(203, '2025-11-11', '11:00', 'Activa', TIME '01:30:00', TRUE, 4, 4); -- Bogotá / Gran Estación
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(204, '2025-11-11', '15:00', 'Activa', TIME '01:30:00', TRUE, 4, 5);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(205, '2025-11-11', '10:30', 'Activa', TIME '01:30:00', TRUE, 4, 8); -- Medellín / Viva Envigado
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(206, '2025-11-11', '17:00', 'Activa', TIME '01:30:00', TRUE, 4, 9);
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES(207, '2025-11-11', '12:00', 'Activa', TIME '01:30:00', TRUE, 4, 10); -- Medellín / El Tesoro

INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(1, 'A', 1, 'Reclinable', TRUE, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(2, 'A', 2, 'Reclinable', TRUE, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(3, 'B', 1, 'Reclinable', TRUE, 1);
INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(4, 'A', 1, 'Estandar',   TRUE, 2);
INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(5, 'A', 2, 'Estandar',   TRUE, 2);
INSERT INTO SILLA (ID, FILA, NUMERO, TIPO, ESTADO_BOOL, SALA_ID) VALUES(6, 'B', 1, 'Estandar',   TRUE, 3);

INSERT INTO COMPRA (ID, TOTAL, FECHA, CLIENTE_ID) VALUES(1, 39.00, '2025-09-15', 2);

INSERT INTO PAGO (ID, METODO, MONTO, ESTADO, ESTADO_BOOL, FECHA, COMPRA_ID) VALUES(1, 'Tarjeta de crédito', 39.00, 'Completado', TRUE, '2025-09-15', 1);

INSERT INTO RESERVA (ID, CODIGO, FECHA_VENCIMIENTO, ESTADO, ESTADO_BOOL, PRECIO_FINAL, CLIENTE_ID, FUNCION_ID) VALUES(1, 'RSV-001', '2025-09-15', 'Pendiente', TRUE, 30.00, 2, 1);

INSERT INTO BOLETO (ID, CODIGO, ESTADO, ESTADO_BOOL, PRECIO_FINAL, COMPRA_ID, FUNCION_ID) VALUES(1, 'BOL-001', 'Usado', FALSE, 15.00, 1, 1);
INSERT INTO BOLETO (ID, CODIGO, ESTADO, ESTADO_BOOL, PRECIO_FINAL, COMPRA_ID, FUNCION_ID) VALUES(2, 'BOL-002', 'Usado', FALSE, 15.00, 1, 1);

--Relaciones
INSERT INTO RESERVA_SILLA (RESERVA_ID, SILLA_ID) VALUES (1, 2);
INSERT INTO RESERVA_SILLA (RESERVA_ID, SILLA_ID) VALUES (1, 3);

INSERT INTO BOLETO_SILLA (BOLETO_ID, SILLA_ID) VALUES (1, 1);
INSERT INTO BOLETO_SILLA (BOLETO_ID, SILLA_ID) VALUES (2, 4);

INSERT INTO COMPRA_PRODUCTO (COMPRA_ID, PRODUCTO_ID, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) VALUES(1, 4, 3,  3.00,  9.00);

INSERT INTO SIGMA_CARD (ID, SALDO, ESTADO) VALUES (2, 50.00, TRUE);
INSERT INTO SIGMA_CARD (ID, SALDO, ESTADO) VALUES (3,  0.00, TRUE);


-- 2025-10-19 - Película 1 (Dune: Part Two)
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(17, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(18, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(19, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(20, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(21, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(22, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(23, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(24, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(25, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(26, '2025-10-19','13:00','Activa', TIME '02:46:00', TRUE, 1,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(27, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(28, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(29, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(30, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(31, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(32, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(33, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(34, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(35, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(36, '2025-10-19','16:00','Activa', TIME '02:46:00', TRUE, 1,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(37, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(38, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(39, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(40, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(41, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(42, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(43, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(44, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(45, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(46, '2025-10-19','19:00','Activa', TIME '02:46:00', TRUE, 1,10);

-- 2025-10-19 - Película 2 (Joker)
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(47, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(48, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(49, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(50, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(51, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(52, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(53, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(54, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(55, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(56, '2025-10-19','13:00','Activa', TIME '02:02:00', TRUE, 2,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(57, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(58, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(59, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(60, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(61, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(62, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(63, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(64, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(65, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(66, '2025-10-19','16:00','Activa', TIME '02:02:00', TRUE, 2,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(67, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(68, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(69, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(70, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(71, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(72, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(73, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(74, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(75, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(76, '2025-10-19','19:00','Activa', TIME '02:02:00', TRUE, 2,10);

-- 2025-10-20 - Película 1 (Dune: Part Two)
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(77, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(78, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(79, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(80, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(81, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(82, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(83, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(84, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(85, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(86, '2025-10-20','13:00','Activa', TIME '02:46:00', TRUE, 1,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(87, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(88, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(89, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(90, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(91, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(92, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(93, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(94, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(95, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(96, '2025-10-20','16:00','Activa', TIME '02:46:00', TRUE, 1,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(97, '2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 1),
(98, '2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 2),
(99, '2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 3),
(100,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 4),
(101,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 5),
(102,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 6),
(103,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 7),
(104,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 8),
(105,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1, 9),
(106,'2025-10-20','19:00','Activa', TIME '02:46:00', TRUE, 1,10);

-- 2025-10-20 - Película 2 (Joker)
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(107,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(108,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(109,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(110,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(111,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(112,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(113,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(114,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(115,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(116,'2025-10-20','13:00','Activa', TIME '02:02:00', TRUE, 2,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(117,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(118,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(119,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(120,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(121,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(122,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(123,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(124,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(125,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(126,'2025-10-20','16:00','Activa', TIME '02:02:00', TRUE, 2,10);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(127,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 1),
(128,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 2),
(129,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 3),
(130,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 4),
(131,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 5),
(132,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 6),
(133,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 7),
(134,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 8),
(136,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2, 9),
(135,'2025-10-20','19:00','Activa', TIME '02:02:00', TRUE, 2,10);

-- Funciones para Godzilla x Kong (PELICULA_ID = 3)
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(300,'2025-10-19','14:30','Activa', TIME '01:55:00', TRUE, 3, 1),
(301,'2025-10-19','17:15','Activa', TIME '01:55:00', TRUE, 3, 2),
(302,'2025-10-19','20:00','Activa', TIME '01:55:00', TRUE, 3, 3),
(303,'2025-10-20','15:00','Activa', TIME '01:55:00', TRUE, 3, 4),
(304,'2025-10-20','18:30','Activa', TIME '01:55:00', TRUE, 3, 5),
(305,'2025-10-20','21:15','Activa', TIME '01:55:00', TRUE, 3, 6);

INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(500,'2025-11-15','12:00','Activa', TIME '01:54:00', TRUE, 5, 1),
(501,'2025-11-15','15:00','Activa', TIME '01:54:00', TRUE, 5, 2),
(502,'2025-11-15','18:00','Activa', TIME '01:54:00', TRUE, 5, 3),
(503,'2025-11-15','21:00','Activa', TIME '01:54:00', TRUE, 5, 4),
(504,'2025-11-15','13:30','Activa', TIME '01:54:00', TRUE, 5, 5),
(505,'2025-11-15','16:30','Activa', TIME '01:54:00', TRUE, 5, 6),
(506,'2025-11-15','19:30','Activa', TIME '01:54:00', TRUE, 5, 7),
(507,'2025-11-15','14:00','Activa', TIME '01:54:00', TRUE, 5, 8),
(508,'2025-11-15','17:00','Activa', TIME '01:54:00', TRUE, 5, 9),
(509,'2025-11-15','20:00','Activa', TIME '01:54:00', TRUE, 5, 10);

-- 2025-11-16 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(510,'2025-11-16','12:00','Activa', TIME '01:54:00', TRUE, 5, 1),
(511,'2025-11-16','15:00','Activa', TIME '01:54:00', TRUE, 5, 2),
(512,'2025-11-16','18:00','Activa', TIME '01:54:00', TRUE, 5, 3),
(513,'2025-11-16','21:00','Activa', TIME '01:54:00', TRUE, 5, 4),
(514,'2025-11-16','13:30','Activa', TIME '01:54:00', TRUE, 5, 5),
(515,'2025-11-16','16:30','Activa', TIME '01:54:00', TRUE, 5, 6),
(516,'2025-11-16','19:30','Activa', TIME '01:54:00', TRUE, 5, 7),
(517,'2025-11-16','14:00','Activa', TIME '01:54:00', TRUE, 5, 8),
(518,'2025-11-16','17:00','Activa', TIME '01:54:00', TRUE, 5, 9),
(519,'2025-11-16','20:00','Activa', TIME '01:54:00', TRUE, 5, 10);

-- Funciones para Frozen 2 (PELICULA_ID = 6) - Duración: 01:43:00
-- 2025-11-15 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(600,'2025-11-15','11:00','Activa', TIME '01:43:00', TRUE, 6, 1),
(601,'2025-11-15','14:00','Activa', TIME '01:43:00', TRUE, 6, 2),
(602,'2025-11-15','17:00','Activa', TIME '01:43:00', TRUE, 6, 3),
(603,'2025-11-15','20:00','Activa', TIME '01:43:00', TRUE, 6, 4),
(604,'2025-11-15','12:30','Activa', TIME '01:43:00', TRUE, 6, 5),
(605,'2025-11-15','15:30','Activa', TIME '01:43:00', TRUE, 6, 6),
(606,'2025-11-15','18:30','Activa', TIME '01:43:00', TRUE, 6, 7),
(607,'2025-11-15','13:00','Activa', TIME '01:43:00', TRUE, 6, 8),
(608,'2025-11-15','16:00','Activa', TIME '01:43:00', TRUE, 6, 9),
(609,'2025-11-15','19:00','Activa', TIME '01:43:00', TRUE, 6, 10);

-- 2025-11-16 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(610,'2025-11-16','11:00','Activa', TIME '01:43:00', TRUE, 6, 1),
(611,'2025-11-16','14:00','Activa', TIME '01:43:00', TRUE, 6, 2),
(612,'2025-11-16','17:00','Activa', TIME '01:43:00', TRUE, 6, 3),
(613,'2025-11-16','20:00','Activa', TIME '01:43:00', TRUE, 6, 4),
(614,'2025-11-16','12:30','Activa', TIME '01:43:00', TRUE, 6, 5),
(615,'2025-11-16','15:30','Activa', TIME '01:43:00', TRUE, 6, 6),
(616,'2025-11-16','18:30','Activa', TIME '01:43:00', TRUE, 6, 7),
(617,'2025-11-16','13:00','Activa', TIME '01:43:00', TRUE, 6, 8),
(618,'2025-11-16','16:00','Activa', TIME '01:43:00', TRUE, 6, 9),
(619,'2025-11-16','19:00','Activa', TIME '01:43:00', TRUE, 6, 10);

-- Funciones para Interstellar (PELICULA_ID = 7) - Duración: 02:49:00
-- 2025-11-17 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(700,'2025-11-17','12:00','Activa', TIME '02:49:00', TRUE, 7, 1),
(701,'2025-11-17','15:30','Activa', TIME '02:49:00', TRUE, 7, 2),
(702,'2025-11-17','19:00','Activa', TIME '02:49:00', TRUE, 7, 3),
(703,'2025-11-17','13:00','Activa', TIME '02:49:00', TRUE, 7, 4),
(704,'2025-11-17','16:30','Activa', TIME '02:49:00', TRUE, 7, 5),
(705,'2025-11-17','20:00','Activa', TIME '02:49:00', TRUE, 7, 6),
(706,'2025-11-17','14:00','Activa', TIME '02:49:00', TRUE, 7, 7),
(707,'2025-11-17','17:30','Activa', TIME '02:49:00', TRUE, 7, 8),
(708,'2025-11-17','21:00','Activa', TIME '02:49:00', TRUE, 7, 9),
(709,'2025-11-17','12:30','Activa', TIME '02:49:00', TRUE, 7, 10);

-- 2025-11-18 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(710,'2025-11-18','12:00','Activa', TIME '02:49:00', TRUE, 7, 1),
(711,'2025-11-18','15:30','Activa', TIME '02:49:00', TRUE, 7, 2),
(712,'2025-11-18','19:00','Activa', TIME '02:49:00', TRUE, 7, 3),
(713,'2025-11-18','13:00','Activa', TIME '02:49:00', TRUE, 7, 4),
(714,'2025-11-18','16:30','Activa', TIME '02:49:00', TRUE, 7, 5),
(715,'2025-11-18','20:00','Activa', TIME '02:49:00', TRUE, 7, 6),
(716,'2025-11-18','14:00','Activa', TIME '02:49:00', TRUE, 7, 7),
(717,'2025-11-18','17:30','Activa', TIME '02:49:00', TRUE, 7, 8),
(718,'2025-11-18','21:00','Activa', TIME '02:49:00', TRUE, 7, 9),
(719,'2025-11-18','12:30','Activa', TIME '02:49:00', TRUE, 7, 10);

-- Funciones para Sharkboy y Lavagirl (PELICULA_ID = 8) - Duración: 01:33:00
-- 2025-11-17 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(800,'2025-11-17','10:30','Activa', TIME '01:33:00', TRUE, 8, 1),
(801,'2025-11-17','13:00','Activa', TIME '01:33:00', TRUE, 8, 2),
(802,'2025-11-17','15:30','Activa', TIME '01:33:00', TRUE, 8, 3),
(803,'2025-11-17','18:00','Activa', TIME '01:33:00', TRUE, 8, 4),
(804,'2025-11-17','11:00','Activa', TIME '01:33:00', TRUE, 8, 5),
(805,'2025-11-17','14:00','Activa', TIME '01:33:00', TRUE, 8, 6),
(806,'2025-11-17','16:30','Activa', TIME '01:33:00', TRUE, 8, 7),
(807,'2025-11-17','19:00','Activa', TIME '01:33:00', TRUE, 8, 8),
(808,'2025-11-17','12:00','Activa', TIME '01:33:00', TRUE, 8, 9),
(809,'2025-11-17','15:00','Activa', TIME '01:33:00', TRUE, 8, 10);

-- 2025-11-18 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(810,'2025-11-18','10:30','Activa', TIME '01:33:00', TRUE, 8, 1),
(811,'2025-11-18','13:00','Activa', TIME '01:33:00', TRUE, 8, 2),
(812,'2025-11-18','15:30','Activa', TIME '01:33:00', TRUE, 8, 3),
(813,'2025-11-18','18:00','Activa', TIME '01:33:00', TRUE, 8, 4),
(814,'2025-11-18','11:00','Activa', TIME '01:33:00', TRUE, 8, 5),
(815,'2025-11-18','14:00','Activa', TIME '01:33:00', TRUE, 8, 6),
(816,'2025-11-18','16:30','Activa', TIME '01:33:00', TRUE, 8, 7),
(817,'2025-11-18','19:00','Activa', TIME '01:33:00', TRUE, 8, 8),
(818,'2025-11-18','12:00','Activa', TIME '01:33:00', TRUE, 8, 9),
(819,'2025-11-18','15:00','Activa', TIME '01:33:00', TRUE, 8, 10);

-- Funciones para No Se Aceptan Devoluciones (PELICULA_ID = 9) - Duración: 02:02:00
-- 2025-11-19 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(900,'2025-11-19','12:00','Activa', TIME '02:02:00', TRUE, 9, 1),
(901,'2025-11-19','15:00','Activa', TIME '02:02:00', TRUE, 9, 2),
(902,'2025-11-19','18:00','Activa', TIME '02:02:00', TRUE, 9, 3),
(903,'2025-11-19','21:00','Activa', TIME '02:02:00', TRUE, 9, 4),
(904,'2025-11-19','13:00','Activa', TIME '02:02:00', TRUE, 9, 5),
(905,'2025-11-19','16:00','Activa', TIME '02:02:00', TRUE, 9, 6),
(906,'2025-11-19','19:00','Activa', TIME '02:02:00', TRUE, 9, 7),
(907,'2025-11-19','14:00','Activa', TIME '02:02:00', TRUE, 9, 8),
(908,'2025-11-19','17:00','Activa', TIME '02:02:00', TRUE, 9, 9),
(909,'2025-11-19','20:00','Activa', TIME '02:02:00', TRUE, 9, 10);

-- 2025-11-20 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(910,'2025-11-20','12:00','Activa', TIME '02:02:00', TRUE, 9, 1),
(911,'2025-11-20','15:00','Activa', TIME '02:02:00', TRUE, 9, 2),
(912,'2025-11-20','18:00','Activa', TIME '02:02:00', TRUE, 9, 3),
(913,'2025-11-20','21:00','Activa', TIME '02:02:00', TRUE, 9, 4),
(914,'2025-11-20','13:00','Activa', TIME '02:02:00', TRUE, 9, 5),
(915,'2025-11-20','16:00','Activa', TIME '02:02:00', TRUE, 9, 6),
(916,'2025-11-20','19:00','Activa', TIME '02:02:00', TRUE, 9, 7),
(917,'2025-11-20','14:00','Activa', TIME '02:02:00', TRUE, 9, 8),
(918,'2025-11-20','17:00','Activa', TIME '02:02:00', TRUE, 9, 9),
(919,'2025-11-20','20:00','Activa', TIME '02:02:00', TRUE, 9, 10);

-- Funciones para Spider-Man: Brand New Day (PELICULA_ID = 10) - Duración: 02:15:00
-- 2025-11-21 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1000,'2025-11-21','12:00','Activa', TIME '02:15:00', TRUE, 10, 1),
(1001,'2025-11-21','15:00','Activa', TIME '02:15:00', TRUE, 10, 2),
(1002,'2025-11-21','18:00','Activa', TIME '02:15:00', TRUE, 10, 3),
(1003,'2025-11-21','21:00','Activa', TIME '02:15:00', TRUE, 10, 4),
(1004,'2025-11-21','13:00','Activa', TIME '02:15:00', TRUE, 10, 5),
(1005,'2025-11-21','16:00','Activa', TIME '02:15:00', TRUE, 10, 6),
(1006,'2025-11-21','19:00','Activa', TIME '02:15:00', TRUE, 10, 7),
(1007,'2025-11-21','14:00','Activa', TIME '02:15:00', TRUE, 10, 8),
(1008,'2025-11-21','17:00','Activa', TIME '02:15:00', TRUE, 10, 9),
(1009,'2025-11-21','20:00','Activa', TIME '02:15:00', TRUE, 10, 10);

-- 2025-11-22 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1010,'2025-11-22','12:00','Activa', TIME '02:15:00', TRUE, 10, 1),
(1011,'2025-11-22','15:00','Activa', TIME '02:15:00', TRUE, 10, 2),
(1012,'2025-11-22','18:00','Activa', TIME '02:15:00', TRUE, 10, 3),
(1013,'2025-11-22','21:00','Activa', TIME '02:15:00', TRUE, 10, 4),
(1014,'2025-11-22','13:00','Activa', TIME '02:15:00', TRUE, 10, 5),
(1015,'2025-11-22','16:00','Activa', TIME '02:15:00', TRUE, 10, 6),
(1016,'2025-11-22','19:00','Activa', TIME '02:15:00', TRUE, 10, 7),
(1017,'2025-11-22','14:00','Activa', TIME '02:15:00', TRUE, 10, 8),
(1018,'2025-11-22','17:00','Activa', TIME '02:15:00', TRUE, 10, 9),
(1019,'2025-11-22','20:00','Activa', TIME '02:15:00', TRUE, 10, 10);

-- Funciones para Star Wars: The Force Awakens (PELICULA_ID = 11) - Duración: 02:18:00
-- 2025-11-23 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1100,'2025-11-23','11:00','Activa', TIME '02:18:00', TRUE, 11, 1),
(1101,'2025-11-23','14:00','Activa', TIME '02:18:00', TRUE, 11, 2),
(1102,'2025-11-23','17:00','Activa', TIME '02:18:00', TRUE, 11, 3),
(1103,'2025-11-23','20:00','Activa', TIME '02:18:00', TRUE, 11, 4),
(1104,'2025-11-23','12:00','Activa', TIME '02:18:00', TRUE, 11, 5),
(1105,'2025-11-23','15:00','Activa', TIME '02:18:00', TRUE, 11, 6),
(1106,'2025-11-23','18:00','Activa', TIME '02:18:00', TRUE, 11, 7),
(1107,'2025-11-23','13:00','Activa', TIME '02:18:00', TRUE, 11, 8),
(1108,'2025-11-23','16:00','Activa', TIME '02:18:00', TRUE, 11, 9),
(1109,'2025-11-23','19:00','Activa', TIME '02:18:00', TRUE, 11, 10);

-- 2025-11-24 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1110,'2025-11-24','11:00','Activa', TIME '02:18:00', TRUE, 11, 1),
(1111,'2025-11-24','14:00','Activa', TIME '02:18:00', TRUE, 11, 2),
(1112,'2025-11-24','17:00','Activa', TIME '02:18:00', TRUE, 11, 3),
(1113,'2025-11-24','20:00','Activa', TIME '02:18:00', TRUE, 11, 4),
(1114,'2025-11-24','12:00','Activa', TIME '02:18:00', TRUE, 11, 5),
(1115,'2025-11-24','15:00','Activa', TIME '02:18:00', TRUE, 11, 6),
(1116,'2025-11-24','18:00','Activa', TIME '02:18:00', TRUE, 11, 7),
(1117,'2025-11-24','13:00','Activa', TIME '02:18:00', TRUE, 11, 8),
(1118,'2025-11-24','16:00','Activa', TIME '02:18:00', TRUE, 11, 9),
(1119,'2025-11-24','19:00','Activa', TIME '02:18:00', TRUE, 11, 10);

-- Funciones para Teen Beach Movie (PELICULA_ID = 12) - Duración: 01:50:00
-- 2025-11-25 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1200,'2025-11-25','10:00','Activa', TIME '01:50:00', TRUE, 12, 1),
(1201,'2025-11-25','13:00','Activa', TIME '01:50:00', TRUE, 12, 2),
(1202,'2025-11-25','16:00','Activa', TIME '01:50:00', TRUE, 12, 3),
(1203,'2025-11-25','19:00','Activa', TIME '01:50:00', TRUE, 12, 4),
(1204,'2025-11-25','11:00','Activa', TIME '01:50:00', TRUE, 12, 5),
(1205,'2025-11-25','14:00','Activa', TIME '01:50:00', TRUE, 12, 6),
(1206,'2025-11-25','17:00','Activa', TIME '01:50:00', TRUE, 12, 7),
(1207,'2025-11-25','12:00','Activa', TIME '01:50:00', TRUE, 12, 8),
(1208,'2025-11-25','15:00','Activa', TIME '01:50:00', TRUE, 12, 9),
(1209,'2025-11-25','18:00','Activa', TIME '01:50:00', TRUE, 12, 10);

-- 2025-11-26 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1210,'2025-11-26','10:00','Activa', TIME '01:50:00', TRUE, 12, 1),
(1211,'2025-11-26','13:00','Activa', TIME '01:50:00', TRUE, 12, 2),
(1212,'2025-11-26','16:00','Activa', TIME '01:50:00', TRUE, 12, 3),
(1213,'2025-11-26','19:00','Activa', TIME '01:50:00', TRUE, 12, 4),
(1214,'2025-11-26','11:00','Activa', TIME '01:50:00', TRUE, 12, 5),
(1215,'2025-11-26','14:00','Activa', TIME '01:50:00', TRUE, 12, 6),
(1216,'2025-11-26','17:00','Activa', TIME '01:50:00', TRUE, 12, 7),
(1217,'2025-11-26','12:00','Activa', TIME '01:50:00', TRUE, 12, 8),
(1218,'2025-11-26','15:00','Activa', TIME '01:50:00', TRUE, 12, 9),
(1219,'2025-11-26','18:00','Activa', TIME '01:50:00', TRUE, 12, 10);

-- Funciones para Violet Evergarden (PELICULA_ID = 13) - Duración: 01:30:00
-- 2025-11-27 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1300,'2025-11-27','11:30','Activa', TIME '01:30:00', TRUE, 13, 1),
(1301,'2025-11-27','14:30','Activa', TIME '01:30:00', TRUE, 13, 2),
(1302,'2025-11-27','17:30','Activa', TIME '01:30:00', TRUE, 13, 3),
(1303,'2025-11-27','20:30','Activa', TIME '01:30:00', TRUE, 13, 4),
(1304,'2025-11-27','12:00','Activa', TIME '01:30:00', TRUE, 13, 5),
(1305,'2025-11-27','15:00','Activa', TIME '01:30:00', TRUE, 13, 6),
(1306,'2025-11-27','18:00','Activa', TIME '01:30:00', TRUE, 13, 7),
(1307,'2025-11-27','13:00','Activa', TIME '01:30:00', TRUE, 13, 8),
(1308,'2025-11-27','16:00','Activa', TIME '01:30:00', TRUE, 13, 9),
(1309,'2025-11-27','19:00','Activa', TIME '01:30:00', TRUE, 13, 10);

-- 2025-11-28 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1310,'2025-11-28','11:30','Activa', TIME '01:30:00', TRUE, 13, 1),
(1311,'2025-11-28','14:30','Activa', TIME '01:30:00', TRUE, 13, 2),
(1312,'2025-11-28','17:30','Activa', TIME '01:30:00', TRUE, 13, 3),
(1313,'2025-11-28','20:30','Activa', TIME '01:30:00', TRUE, 13, 4),
(1314,'2025-11-28','12:00','Activa', TIME '01:30:00', TRUE, 13, 5),
(1315,'2025-11-28','15:00','Activa', TIME '01:30:00', TRUE, 13, 6),
(1316,'2025-11-28','18:00','Activa', TIME '01:30:00', TRUE, 13, 7),
(1317,'2025-11-28','13:00','Activa', TIME '01:30:00', TRUE, 13, 8),
(1318,'2025-11-28','16:00','Activa', TIME '01:30:00', TRUE, 13, 9),
(1319,'2025-11-28','19:00','Activa', TIME '01:30:00', TRUE, 13, 10);

-- Funciones para WALL-E (PELICULA_ID = 14) - Duración: 01:38:00
-- 2025-11-29 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1400,'2025-11-29','10:30','Activa', TIME '01:38:00', TRUE, 14, 1),
(1401,'2025-11-29','13:30','Activa', TIME '01:38:00', TRUE, 14, 2),
(1402,'2025-11-29','16:30','Activa', TIME '01:38:00', TRUE, 14, 3),
(1403,'2025-11-29','19:30','Activa', TIME '01:38:00', TRUE, 14, 4),
(1404,'2025-11-29','11:00','Activa', TIME '01:38:00', TRUE, 14, 5),
(1405,'2025-11-29','14:00','Activa', TIME '01:38:00', TRUE, 14, 6),
(1406,'2025-11-29','17:00','Activa', TIME '01:38:00', TRUE, 14, 7),
(1407,'2025-11-29','12:00','Activa', TIME '01:38:00', TRUE, 14, 8),
(1408,'2025-11-29','15:00','Activa', TIME '01:38:00', TRUE, 14, 9),
(1409,'2025-11-29','18:00','Activa', TIME '01:38:00', TRUE, 14, 10);

-- 2025-11-30 en todas las salas
INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) VALUES
(1410,'2025-11-30','10:30','Activa', TIME '01:38:00', TRUE, 14, 1),
(1411,'2025-11-30','13:30','Activa', TIME '01:38:00', TRUE, 14, 2),
(1412,'2025-11-30','16:30','Activa', TIME '01:38:00', TRUE, 14, 3),
(1413,'2025-11-30','19:30','Activa', TIME '01:38:00', TRUE, 14, 4),
(1414,'2025-11-30','11:00','Activa', TIME '01:38:00', TRUE, 14, 5),
(1415,'2025-11-30','14:00','Activa', TIME '01:38:00', TRUE, 14, 6),
(1416,'2025-11-30','17:00','Activa', TIME '01:38:00', TRUE, 14, 7),
(1417,'2025-11-30','12:00','Activa', TIME '01:38:00', TRUE, 14, 8),
(1418,'2025-11-30','15:00','Activa', TIME '01:38:00', TRUE, 14, 9),
(1419,'2025-11-30','18:00','Activa', TIME '01:38:00', TRUE, 14, 10);
