delete from transition where locfrom in (select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'Сказка') or locto in (select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'Сказка');

delete from habitation where location in (select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'Сказка');

delete from disposition where location in (select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'Сказка');

delete from location where id in (
select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'Сказка');
