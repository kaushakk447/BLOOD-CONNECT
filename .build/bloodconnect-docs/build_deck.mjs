import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const workspaceDir = "C:\\APP\\bloodconnect-app-fixed-2\\blood connect";
const SKILL_DIR = "C:\\Users\\amirt\\.codex\\plugins\\cache\\openai-primary-runtime\\presentations\\26.904.11930\\skills\\presentations";
const TMP_DIR = path.join(workspaceDir, ".build", "bloodconnect-docs");
const FINAL_PPTX = path.join(workspaceDir, "output", "pptx", "BloodConnect_Technical_Overview.pptx");
const RUNTIME_PYTHON = "C:\\Users\\amirt\\.cache\\codex-runtimes\\codex-primary-runtime\\dependencies\\python\\python.exe";
const { resolvePresentationFont, finalizePresentation } = await import(pathToFileURL(path.join(SKILL_DIR, "container_tools/artifact_tool_utils.mjs")).href);
const family = resolvePresentationFont();
const p = Presentation.create({slideSize:{width:1280,height:720}});
const navy="#0B3157", red="#B42335", coral="#FCE7EA", slate="#425466", pale="#F7FAFC";
function text(slide, value, left, top, width, height, size=24, color=navy, bold=false) { const s=slide.shapes.add({geometry:"textbox",position:{left,top,width,height},fill:"none",line:{fill:"none",width:0}}); s.text=value; s.text.style={typeface:family,fontSize:size,color,bold,autoFit:"shrinkText"}; return s; }
function line(slide,x,y,w,color=red){slide.shapes.add({geometry:"rect",position:{left:x,top:y,width:w,height:6},fill:color,line:{fill:"none",width:0}})}
function slideBase(title, num){const s=p.slides.add();s.background.fill="#FFFFFF"; text(s,title,70,48,1080,55,34,navy,true);line(s,70,118,120); text(s,`BloodConnect  |  ${num}`,1050,52,150,22,12,slate,false);return s;}
function bullet(slide, y, head, body){text(slide,head,90,y,310,28,18,navy,true);text(slide,body,90,y+32,1030,55,16,slate,false);}

let s=p.slides.add(); s.background.fill="#FFFFFF"; s.images.add({blob:await fs.readFile(path.join(TMP_DIR,"bloodconnect-cover.png")),contentType:"image/png",position:{left:0,top:0,width:1280,height:720},fit:"cover"}); text(s,"BloodConnect",70,155,470,65,44,navy,true); text(s,"Technical overview and implementation guide",70,230,470,55,23,slate,false); text(s,"React • Spring Boot • PostgreSQL • Docker",70,306,450,30,18,red,true);

s=slideBase("Platform architecture", "02"); text(s,"Browser",95,190,210,30,23,navy,true); text(s,"React + Vite\nRoutes, forms, auth state",95,232,210,70,17,slate,false); text(s,"REST API",520,190,210,30,23,navy,true); text(s,"Spring Boot\nControllers, services, JWT",520,232,210,70,17,slate,false); text(s,"Database",930,190,210,30,23,navy,true); text(s,"PostgreSQL + Flyway\nPersistence and migrations",930,232,240,70,17,slate,false); line(s,315,254,165,red); line(s,740,254,145,red); text(s,"The frontend calls a secured API under /api. The backend keeps business rules out of the UI and persists core records through JPA.",90,430,1050,65,23,slate,false);

s=slideBase("Frontend application", "03"); bullet(s,170,"App.jsx","Hydrates the Zustand authentication store and selects public or protected routes."); bullet(s,285,"Pages","Screens cover login, registration, request creation and tracking, donor registration, notifications and administration."); bullet(s,400,"Service layer","Axios calls belong in frontend/src/services/api.js so endpoint configuration stays separate from view components."); text(s,"Routes are protected by the presence of an authenticated user.",90,545,1020,32,21,red,true);

s=slideBase("API endpoints and authentication", "04"); text(s,"Authentication",90,170,330,35,23,navy,true); text(s,"POST /api/auth/register\nPOST /api/auth/login\nPOST /api/auth/refresh\nGET /api/auth/health",90,220,350,155,18,slate,false); text(s,"Blood requests",600,170,330,35,23,navy,true); text(s,"POST /api/blood-requests\nGET /api/blood-requests/{id}\nGET /api/blood-requests\nPATCH /api/blood-requests/{id}",600,220,440,155,18,slate,false); text(s,"The JWT filter identifies the caller. Controllers read the user ID and delegate validation and business rules to services.",90,475,1060,55,21,red,true);

s=slideBase("Donor matching workflow", "05"); text(s,"1  Request created",90,190,240,32,21,navy,true); text(s,"Requested blood group, location and radius",90,228,220,58,16,slate,false); text(s,"2  Eligible donors",440,190,240,32,21,navy,true); text(s,"Verified + available + compatible group",440,228,250,58,16,slate,false); text(s,"3  Nearby donors",815,190,240,32,21,navy,true); text(s,"Haversine distance filter and proximity sort",815,228,270,58,16,slate,false); line(s,330,244,75,red); line(s,700,244,75,red); text(s,"MatchingService uses a compatibility map and an Earth radius of 6,371 km. For high volume, move radius search into a geospatial database query.",90,440,1040,60,22,slate,false);

s=slideBase("Database model", "06"); text(s,"users",105,190,180,32,23,navy,true); text(s,"Identity, role, account status",105,230,190,45,16,slate,false); text(s,"donor_profiles",410,190,230,32,23,navy,true); text(s,"Eligibility, blood group, location",410,230,240,45,16,slate,false); text(s,"blood_requests",760,190,230,32,23,navy,true); text(s,"Demand, urgency, location, status",760,230,250,45,16,slate,false); line(s,285,238,95,red); line(s,655,238,70,red); text(s,"donor_requests records each matched donor and response. notifications inform users. audit_logs retain an activity trail.",105,420,980,48,22,red,true); text(s,"Flyway migration V1 creates tables and indexes. Add future changes as new migrations rather than modifying applied files.",105,500,980,42,19,slate,false);

s=slideBase("Local development", "07"); text(s,"Quick start",90,170,280,34,24,navy,true); text(s,"docker compose up --build",90,220,440,38,23,red,true); bullet(s,310,"Frontend","Open http://localhost:3000 after the frontend container starts."); bullet(s,405,"Health check","Open http://localhost:8080/api/auth/health to verify the API."); bullet(s,500,"Configuration","Use DB_URL, DB_USERNAME, DB_PASSWORD and JWT_SECRET for environment-specific configuration.");

s=slideBase("Production readiness", "08"); bullet(s,170,"Secrets and transport","Replace default JWT secrets, use HTTPS and restrict CORS to real deployment origins."); bullet(s,285,"Testing","Cover registration, login, rejected tokens, request creation and donor matching with automated tests."); bullet(s,400,"Scale and safety","Use geospatial database querying for growth. Get clinical and legal approval for blood compatibility and donor eligibility rules."); text(s,"Engineering foundation complete. Operational validation remains essential before real-world deployment.",90,555,1070,33,21,red,true);

await fs.mkdir(TMP_DIR,{recursive:true}); await fs.mkdir(path.dirname(FINAL_PPTX),{recursive:true});
const candidatePath=path.join(TMP_DIR,"bloodconnect-candidate.pptx"); await (await PresentationFile.exportPptx(p)).save(candidatePath);
const result=await finalizePresentation({workspaceDir,candidatePath,finalPath:FINAL_PPTX,pythonExecutable:RUNTIME_PYTHON,integrityValidatorPath:path.join(SKILL_DIR,"container_tools/inspect_presentation_package_integrity.py"),layoutValidatorPath:path.join(SKILL_DIR,"container_tools/inspect_presentation_layout_geometry.py"),layoutArgs:["--expected-slide-size-emu","12192000,6858000","--validate-bullet-geometry","--validate-heading-fit"],fontPolicy:{basis:"design",families:[family]},verifyArtifactToolImport:true,receiptPath:path.join(TMP_DIR,"bloodconnect.validation.json")});
console.log(JSON.stringify(result));
