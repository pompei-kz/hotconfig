### Обзор

В PassManager есть большой файл для мавена

Там есть токены для [https://central.sonatype.com/usertoken](https://central.sonatype.com/usertoken).

Так же пара ключей для публикации в мавен — публичный ключ и приватный.

И пароль от приватного ключа.

### Формирование файла: `~/.jreleaser/config.properties`

Вначале нужно из файла вырезать публичный ключ и приватный ключ в отдельные файлы.

Пусть будут эти файлы такими:

    goto_maven_public_key.asc
    goto_maven_private_key.asc

Файл `goto_maven_public_key.asc` должен начинаться с `-----BEGIN PGP PUBLIC KEY BLOCK-----`.

Файл `goto_maven_private_key.asc` должен начинаться с `-----BEGIN PGP PRIVATE KEY BLOCK-----`.

И заканчиваются они аналогично.

Дальше их нужно превратить в длинные строки, которые потом вставить в файл. Получаются они так:

    awk '{printf "%s\\n", $0}' goto_maven_public_key.asc
    awk '{printf "%s\\n", $0}' goto_maven_private_key.asc

Полученные строки нужно вставить в файл: `~/.jreleaser/config.properties` вот так:

    JRELEASER_GITHUB_TOKEN=not-used
    JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME=(здесь username токена)
    JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD=(здесь password токена)
    JRELEASER_GPG_PASSPHRASE=(Здесь пароль приватного ключа, который ниже)
    JRELEASER_GPG_PUBLIC_KEY=-----BEGIN PGP PUBLIC KEY BLOCK-----\n\nр3р4м324....dffkjQc=\n=P2EG\n-----END PGP PUBLIC KEY BLOCK-----\n
    JRELEASER_GPG_SECRET_KEY=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\nlQdGBGo....q3\n-----END PGP PRIVATE KEY BLOCK-----\n

В том файле из PassManager будет токены мавена:

    Это токен в мавене
    
    <server>
        <id>${server}</id>
        <username>(здесь username токена)</username>
        <password>(здесь password токена)</password>
    </server>

И ниже в этом файле будет пароль для приватного ключа

### Формирование файла: `~/.gradle/gradle.properties`

Файл должен выглядеть так:

    signing.gnupg.keyName=(Идентификатор ключа в GPG)
    signing.gnupg.executable=gpg
    signing.gnupg.passphrase=(Пароль приватного ключа)

Файлы `goto_maven_public_key.asc` и  `goto_maven_private_key.asc` нужно импортировать в GPG.
Потом получить идентификатор ключа в GPG командой:

    gpg -k

Там будет что-то типа:
    
    /home/pompei/.gnupg/pubring.kbx
    -------------------------------
    pub   rsa4096/0x3BCCCCCCCCCC01 2026-05-20 [SC]
      722DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD1
    uid                   [ultimate] Evgenij Kolpakov (For publications to Maven Central Repository) <pompei@mail.ru>
    sub   rsa4096/0xGGGGGGGGGGGGGGGGGG 2026-05-20 [E]

Идентификатор ключа в GPG вот: 3BCCCCCCCCCC01

`uid` здесь не изменён — если ключей много, то ориентируйся на этот `uid`.
