delete from location where id in (
select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'На реке');

delete from transition where locFrom not in (select id from location) or locTo not in(select id from location);
delete from habitation t where t.location not in (select id from location);
delete from disposition t where t.location not in (select id from location);

