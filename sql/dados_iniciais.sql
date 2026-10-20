-- Dados iniciais opcionais. Usuário: admin / senha: admin123 (troque após o primeiro acesso).
USE cafe_gestor;

INSERT INTO usuario (nome, login, senha, tipo)
VALUES ('Administrador', 'admin', 'pbkdf2$65536$EDtTWX+435KU0G2UkcWBEw==$PynB7SI1sfOieesDFQiK6I7/UTlfaO0SvCv+cqwHQbU=', 'ADMIN');

INSERT INTO tipo_cafe (nome, variedade) VALUES ('Arábica', 'Especial');
INSERT INTO funcionario (nome, funcao, contato) VALUES ('João', 'Colhedor', '99999-9999');
