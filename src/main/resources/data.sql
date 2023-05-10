insert into application_user (version, id, username, name, hashed_password, avatar_image, avatar_image_name, email,
                              enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired,
                              image_size, image_size_limit)
values (1, '1000', 'admin', 'admin',
        '{argon2}$argon2id$v=19$m=16384,t=2,p=1$S9swkCEbXj5O8cwbZdyrtQ$4RkxTGl3xkEfm51P/gc3FORCysPStozAsm6smOnQR18',
        null, 'adminImage', 'admin@example.com', true, true, true, true, 0, 10);
insert into user_roles (user_id, roles)
values ('1000', 'USER');
insert into user_roles (user_id, roles)
values ('1000', 'ADMIN');