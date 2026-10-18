import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";
const root = "C:\\APP\\bloodconnect-app-fixed-2\\blood connect";
const source = path.join(root, "output", "pptx", "BloodConnect_Technical_Overview.pptx");
const out = path.join(root, ".build", "bloodconnect-docs", "previews");
await fs.mkdir(out,{recursive:true});
const deck = await PresentationFile.importPptx(await FileBlob.load(source));
for (let i=0; i<8; i++) {
  const image = await deck.export({slide:deck.slides.getItem(i),format:"png",scale:1});
  await fs.writeFile(path.join(out,`slide-${i+1}.png`),new Uint8Array(await image.arrayBuffer()));
}
console.log(out);
