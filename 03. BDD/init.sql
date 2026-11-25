-- Datos iniciales de APIPaisesDB. Se ejecuta después de APIPaisesDB.sql.
--
-- Usuarios de prueba (usuario / clave):
--   admin / admin123      Administrador
--   msosa / msosa         Mateo Sosa
--
-- Las claves se guardan como SHA-256 en hexadecimal (64 caracteres). SHA2(..., 256) de
-- MySQL produce el mismo valor que HashUtil.sha256 del servidor, así que aquí se calcula
-- con la función en vez de pegar el hash.
--
-- Los países no se cargan: se obtienen de la API REST Countries con «Importar» (ECUD2).

use APIPaisesDB;

set names utf8mb4;

-- Regiones válidas (ECUD, parámetros generales). El orden de inserción no importa:
-- el servidor las busca por nombre.
insert into Regiones (Nombre) values
('América'),
('Europa'),
('Asia'),
('África'),
('Oceanía'),
('Antártida');

insert into Usuarios (Usuario, ClaveHash, Nombre) values
('admin', sha2('admin123', 256), 'Administrador'),
('msosa', sha2('msosa', 256), 'Mateo Sosa');
