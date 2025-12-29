const fs = require('fs');
const parser = require('@babel/parser');

const code = fs.readFileSync(0, 'utf-8'); // liest stdin

// TSX (TypeScript + JSX) parsen
const ast = parser.parse(code, {
  sourceType: 'module',
  plugins: ['typescript', 'jsx'],
  tokens: true
});

// Tokens als JSON ausgeben
console.log(JSON.stringify(ast.tokens));