delete from transition where locFrom in (select id from location where zone is null) or locTo in(select id from location where zone is null)
