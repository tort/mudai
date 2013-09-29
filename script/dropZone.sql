delete from disposition where id in (
select distinct h.id from disposition h join location l on h.location = l.id join zone z on l.zone = z.id join item m on h.item = m.id
where z.name = 'На реке');

delete from habitation where id in (
select distinct h.id from habitation h join location l on h.location = l.id join zone z on l.zone = z.id join mob m on h.mob = m.id
where z.name = 'На реке');

delete from transition where id in (
select distinct t.id from transition t join location l on (t.locFrom = l.id or t.locTo = l.id) join zone z on l.zone = z.id
where z.name = 'На реке');

delete from location where id in (
select distinct l.id from location l join zone z on l.zone = z.id
where z.name = 'На реке');
