/*==============================================================*/
/* Base de datos: APIPaisesDB                                   */
/* DBMS:          MySQL 8.0.16 o superior (usa CHECK)           */
/* Sistema:       APIPaises (TI.2.1. Taller APIs públicas en    */
/*                Java y MySQL, NRC 36883)                      */
/* Fuente:        ERS §9.1.5, ECUD2-ECUD9, diagrama de clases   */
/*                Servidor_APIPaises                            */
/*==============================================================*/
/* Crea la base y las tablas. Los datos iniciales están en      */
/* init.sql, que se ejecuta después de este script.             */
/*                                                              */
/* Collation utf8mb4_0900_ai_ci: no distingue mayúsculas ni     */
/* tildes, así que la búsqueda «quito» encuentra «Quito» y el   */
/* nombre del país es único aunque cambie la tilde (ECUD5).     */
/*==============================================================*/

create database if not exists APIPaisesDB
   character set utf8mb4
   collate utf8mb4_0900_ai_ci;

use APIPaisesDB;

drop table if exists Paises;

drop table if exists Regiones;

drop table if exists Usuarios;

/*==============================================================*/
/* Table: Regiones                                              */
/* Catálogo de las seis regiones en español. ConsumoAPIPaises   */
/* traduce la región de la API y RegionDAO la busca por nombre. */
/*==============================================================*/
create table Regiones
(
   IdRegion             int not null auto_increment,
   Nombre               varchar(20) not null,
   primary key (IdRegion),
   unique key AK_REGIONES_NOMBRE (Nombre)
) engine = InnoDB;

/*==============================================================*/
/* Table: Paises                                                */
/* El nombre en español es la clave primaria (RNF-006). Los     */
/* países sin capital o sin moneda se guardan con «Sin capital» */
/* o «Sin moneda» (ECUD9), por eso ninguna columna es nula.     */
/*==============================================================*/
create table Paises
(
   Nombre               varchar(100) not null,
   Capital              varchar(100) not null,
   IdRegion             int not null,
   Poblacion            bigint not null,
   Moneda               varchar(100) not null,
   primary key (Nombre),
   key IX_PAISES_REGION (IdRegion),
   key IX_PAISES_POBLACION (Poblacion),
   constraint CK_PAISES_POBLACION check (Poblacion >= 0),
   constraint FK_PAISES_REGIONES foreign key (IdRegion)
      references Regiones (IdRegion)
      on update restrict
      on delete restrict
) engine = InnoDB;

/*==============================================================*/
/* Table: Usuarios                                              */
/* Solo se cargan por script (no hay mantenimiento de usuarios).*/
/* ClaveHash = SHA-256 de la clave en hexadecimal (64 car.).    */
/*==============================================================*/
create table Usuarios
(
   IdUsuario            int not null auto_increment,
   Usuario              varchar(50) not null,
   ClaveHash            char(64) not null,
   Nombre               varchar(100) not null,
   primary key (IdUsuario),
   unique key AK_USUARIOS_USUARIO (Usuario)
) engine = InnoDB;
