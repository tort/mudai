select count(*) from transition t1 join transition t2 where t1.locfrom = t2.locfrom and t1.locto = t2.locto and t1.id <> t2.id
