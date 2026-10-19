// Membuat folder www/ yang 100% offline: React, htm, font, dan CSS Tailwind semuanya lokal.
import fs from 'fs';
import path from 'path';
const nm = (p) => path.join('node_modules', p);
fs.rmSync('www', { recursive: true, force: true });
fs.mkdirSync('www/fonts', { recursive: true });
fs.copyFileSync(nm('react/umd/react.production.min.js'), 'www/react.min.js');
fs.copyFileSync(nm('react-dom/umd/react-dom.production.min.js'), 'www/react-dom.min.js');
fs.copyFileSync(nm('htm/dist/htm.umd.js'), 'www/htm.js');

let css = '';
const fonts = { inter: [400, 500, 600, 700], sora: [500, 600, 700, 800] };
for (const [f, weights] of Object.entries(fonts)) {
  const base = nm('@fontsource/' + f);
  for (const w of weights) {
    css += fs.readFileSync(path.join(base, `latin-${w}.css`), 'utf8').replaceAll('./files/', './fonts/');
    for (const ext of ['woff2', 'woff']) {
      const n = `${f}-latin-${w}-normal.${ext}`;
      fs.copyFileSync(path.join(base, 'files', n), path.join('www/fonts', n));
    }
  }
}
fs.writeFileSync('www/fonts.css', css);

let h = fs.readFileSync('src/index.html', 'utf8');
const before = h;
h = h
  .replace(/<link href="https:\/\/fonts\.googleapis[^>]*>/, '<link rel="stylesheet" href="fonts.css">')
  .replace(/<script src="https:\/\/cdn\.tailwindcss[^>]*><\/script>/, '<link rel="stylesheet" href="tailwind.css">')
  .replace(/<script src="https:\/\/cdnjs[^"]*\/react\.production[^>]*><\/script>/, '<script src="react.min.js"></script>')
  .replace(/<script src="https:\/\/cdnjs[^"]*\/react-dom\.production[^>]*><\/script>/, '<script src="react-dom.min.js"></script>')
  .replace(/<script src="https:\/\/cdn\.jsdelivr[^"]*htm[^>]*><\/script>/, '<script src="htm.js"></script>');
if (/src="https?:|href="https?:/.test(h)) throw new Error('Masih ada link internet di index.html');
fs.writeFileSync('www/index.html', h);
console.log('www/ siap (offline)');
