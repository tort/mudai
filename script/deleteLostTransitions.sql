delete from location where zone is null;
delete from transition where locFrom not in (select id from location) or locTo not in(select id from location);
delete from habitation t where t.location not in (select id from location);
delete from disposition t where t.location not in (select id from location);

