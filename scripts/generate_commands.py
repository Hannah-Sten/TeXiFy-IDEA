command_list = ['link', 'text', 'first', 'plural', 'firstplural', 'name', 'symbol', 'desc', 'useri', 'userii', 'useriii', 'useriv', 'userv', 'uservi']

for suffix in command_list:
    for (prefix, namesuffix) in [('gls', ''), ('Gls', 'UPPER'), ('GLS', 'CAPS')]:
        command = prefix + suffix
        name = f'{command.upper()}{namesuffix}'
        print(f'{name}("{command}", "options".asOptional(), "label".asRequired(Argument.Type.TEXT), "insert".asOptional(), dependency = LatexPackage.GLOSSARIES),')