(()=>{function e(e,t,s,r){Object.defineProperty(e,t,{get:s,set:r,enumerable:!0,configurable:!0})}var t=globalThis,s={},r={},n=t.parcelRequire9829;null==n&&((n=function(e){if(e in s)return s[e].exports;if(e in r){var t=r[e];delete r[e];var n={id:e,exports:{}};return s[e]=n,t.call(n.exports,n,n.exports),n.exports}var a=Error("Cannot find module '"+e+"'");throw a.code="MODULE_NOT_FOUND",a}).register=function(e,t){r[e]=t},t.parcelRequire9829=n);var a=n.register;a("aPsVO",function(t,s){e(t.exports,"html",()=>i),e(t.exports,"render",()=>n("cFPH9").render);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("iUyab"),a=n("eDnSP");n("iYB8H"),n("ixdbG"),n("gIbhF"),n("3jJxF"),n("cFPH9"),n("jZRBD"),n("lUGvn"),n("8AGqo"),"undefined"!=typeof window&&(window.litHtmlVersions||(window.litHtmlVersions=[])).push("1.4.1");let i=(e,...t)=>new a.TemplateResult(e,t,"html",r.defaultTemplateProcessor)}),a("iUyab",function(t,s){e(t.exports,"defaultTemplateProcessor",()=>a);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("3jJxF");let a=new class{handleAttributeExpressions(e,t,s,n){let a=t[0];return"."===a?new r.PropertyCommitter(e,t.slice(1),s).parts:"@"===a?[new r.EventPart(e,t.slice(1),n.eventContext)]:"?"===a?[new r.BooleanAttributePart(e,t.slice(1),s)]:new r.AttributeCommitter(e,t,s).parts}handleTextExpression(e){return new r.NodePart(e)}}}),a("3jJxF",function(t,s){e(t.exports,"AttributeCommitter",()=>h),e(t.exports,"NodePart",()=>m),e(t.exports,"BooleanAttributePart",()=>g),e(t.exports,"PropertyCommitter",()=>f),e(t.exports,"EventPart",()=>x);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("iYB8H"),a=n("ixdbG"),i=n("gIbhF"),o=n("lUGvn"),l=n("eDnSP"),c=n("8AGqo");let u=e=>null===e||!("object"==typeof e||"function"==typeof e),d=e=>Array.isArray(e)||!!(e&&e[Symbol.iterator]);class h{constructor(e,t,s){this.dirty=!0,this.element=e,this.name=t,this.strings=s,this.parts=[];for(let e=0;e<s.length-1;e++)this.parts[e]=this._createPart()}_createPart(){return new p(this)}_getValue(){let e=this.strings,t=e.length-1,s=this.parts;if(1===t&&""===e[0]&&""===e[1]){let e=s[0].value;if("symbol"==typeof e)return String(e);if("string"==typeof e||!d(e))return e}let r="";for(let n=0;n<t;n++){r+=e[n];let t=s[n];if(void 0!==t){let e=t.value;if(u(e)||!d(e))r+="string"==typeof e?e:String(e);else for(let t of e)r+="string"==typeof t?t:String(t)}}return r+e[t]}commit(){this.dirty&&(this.dirty=!1,this.element.setAttribute(this.name,this._getValue()))}}class p{constructor(e){this.value=void 0,this.committer=e}setValue(e){e===i.noChange||u(e)&&e===this.value||(this.value=e,(0,r.isDirective)(e)||(this.committer.dirty=!0))}commit(){for(;(0,r.isDirective)(this.value);){let e=this.value;this.value=i.noChange,e(this)}this.value!==i.noChange&&this.committer.commit()}}class m{constructor(e){this.value=void 0,this.__pendingValue=void 0,this.options=e}appendInto(e){this.startNode=e.appendChild((0,c.createMarker)()),this.endNode=e.appendChild((0,c.createMarker)())}insertAfterNode(e){this.startNode=e,this.endNode=e.nextSibling}appendIntoPart(e){e.__insert(this.startNode=(0,c.createMarker)()),e.__insert(this.endNode=(0,c.createMarker)())}insertAfterPart(e){e.__insert(this.startNode=(0,c.createMarker)()),this.endNode=e.endNode,e.endNode=this.startNode}setValue(e){this.__pendingValue=e}commit(){if(null===this.startNode.parentNode)return;for(;(0,r.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}let e=this.__pendingValue;e!==i.noChange&&(u(e)?e!==this.value&&this.__commitText(e):e instanceof l.TemplateResult?this.__commitTemplateResult(e):e instanceof Node?this.__commitNode(e):d(e)?this.__commitIterable(e):e===i.nothing?(this.value=i.nothing,this.clear()):this.__commitText(e))}__insert(e){this.endNode.parentNode.insertBefore(e,this.endNode)}__commitNode(e){this.value!==e&&(this.clear(),this.__insert(e),this.value=e)}__commitText(e){let t=this.startNode.nextSibling,s="string"==typeof(e=null==e?"":e)?e:String(e);t===this.endNode.previousSibling&&3===t.nodeType?t.data=s:this.__commitNode(document.createTextNode(s)),this.value=e}__commitTemplateResult(e){let t=this.options.templateFactory(e);if(this.value instanceof o.TemplateInstance&&this.value.template===t)this.value.update(e.values);else{let s=new o.TemplateInstance(t,e.processor,this.options),r=s._clone();s.update(e.values),this.__commitNode(r),this.value=s}}__commitIterable(e){let t;Array.isArray(this.value)||(this.value=[],this.clear());let s=this.value,r=0;for(let n of e)void 0===(t=s[r])&&(t=new m(this.options),s.push(t),0===r?t.appendIntoPart(this):t.insertAfterPart(s[r-1])),t.setValue(n),t.commit(),r++;r<s.length&&(s.length=r,this.clear(t&&t.endNode))}clear(e=this.startNode){(0,a.removeNodes)(this.startNode.parentNode,e.nextSibling,this.endNode)}}class g{constructor(e,t,s){if(this.value=void 0,this.__pendingValue=void 0,2!==s.length||""!==s[0]||""!==s[1])throw Error("Boolean attributes can only contain a single expression");this.element=e,this.name=t,this.strings=s}setValue(e){this.__pendingValue=e}commit(){for(;(0,r.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}if(this.__pendingValue===i.noChange)return;let e=!!this.__pendingValue;this.value!==e&&(e?this.element.setAttribute(this.name,""):this.element.removeAttribute(this.name),this.value=e),this.__pendingValue=i.noChange}}class f extends h{constructor(e,t,s){super(e,t,s),this.single=2===s.length&&""===s[0]&&""===s[1]}_createPart(){return new v(this)}_getValue(){return this.single?this.parts[0].value:super._getValue()}commit(){this.dirty&&(this.dirty=!1,this.element[this.name]=this._getValue())}}class v extends p{}let _=!1;(()=>{try{let e={get capture(){return _=!0,!1}};window.addEventListener("test",e,e),window.removeEventListener("test",e,e)}catch(e){}})();class x{constructor(e,t,s){this.value=void 0,this.__pendingValue=void 0,this.element=e,this.eventName=t,this.eventContext=s,this.__boundHandleEvent=e=>this.handleEvent(e)}setValue(e){this.__pendingValue=e}commit(){for(;(0,r.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}if(this.__pendingValue===i.noChange)return;let e=this.__pendingValue,t=this.value,s=null==e||null!=t&&(e.capture!==t.capture||e.once!==t.once||e.passive!==t.passive),n=null!=e&&(null==t||s);s&&this.element.removeEventListener(this.eventName,this.__boundHandleEvent,this.__options),n&&(this.__options=b(e),this.element.addEventListener(this.eventName,this.__boundHandleEvent,this.__options)),this.value=e,this.__pendingValue=i.noChange}handleEvent(e){"function"==typeof this.value?this.value.call(this.eventContext||this.element,e):this.value.handleEvent(e)}}let b=e=>e&&(_?{capture:e.capture,passive:e.passive,once:e.once}:e.capture)}),a("iYB8H",function(t,s){e(t.exports,"isDirective",()=>n);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let r=new WeakMap,n=e=>"function"==typeof e&&r.has(e)}),a("ixdbG",function(t,s){e(t.exports,"isCEPolyfill",()=>r),e(t.exports,"reparentNodes",()=>n),e(t.exports,"removeNodes",()=>a);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let r="undefined"!=typeof window&&null!=window.customElements&&void 0!==window.customElements.polyfillWrapFlushCallback,n=(e,t,s=null,r=null)=>{for(;t!==s;){let s=t.nextSibling;e.insertBefore(t,r),t=s}},a=(e,t,s=null)=>{for(;t!==s;){let s=t.nextSibling;e.removeChild(t),t=s}}}),a("gIbhF",function(t,s){e(t.exports,"noChange",()=>r),e(t.exports,"nothing",()=>n);/**
 * @license
 * Copyright (c) 2018 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let r={},n={}}),a("lUGvn",function(t,s){e(t.exports,"TemplateInstance",()=>i);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("ixdbG"),a=n("8AGqo");class i{constructor(e,t,s){this.__parts=[],this.template=e,this.processor=t,this.options=s}update(e){let t=0;for(let s of this.__parts)void 0!==s&&s.setValue(e[t]),t++;for(let e of this.__parts)void 0!==e&&e.commit()}_clone(){let e;let t=r.isCEPolyfill?this.template.element.content.cloneNode(!0):document.importNode(this.template.element.content,!0),s=[],n=this.template.parts,i=document.createTreeWalker(t,133,null,!1),o=0,l=0,c=i.nextNode();for(;o<n.length;){if(e=n[o],!(0,a.isTemplatePartActive)(e)){this.__parts.push(void 0),o++;continue}for(;l<e.index;)l++,"TEMPLATE"===c.nodeName&&(s.push(c),i.currentNode=c.content),null===(c=i.nextNode())&&(i.currentNode=s.pop(),c=i.nextNode());if("node"===e.type){let e=this.processor.handleTextExpression(this.options);e.insertAfterNode(c.previousSibling),this.__parts.push(e)}else this.__parts.push(...this.processor.handleAttributeExpressions(c,e.name,e.strings,this.options));o++}return r.isCEPolyfill&&(document.adoptNode(t),customElements.upgrade(t)),t}}}),a("8AGqo",function(t,s){e(t.exports,"marker",()=>r),e(t.exports,"nodeMarker",()=>n),e(t.exports,"boundAttributeSuffix",()=>i),e(t.exports,"Template",()=>o),e(t.exports,"lastAttributeNameRegex",()=>d),e(t.exports,"createMarker",()=>u),e(t.exports,"isTemplatePartActive",()=>c);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */let r=`{{lit-${String(Math.random()).slice(2)}}}`,n=`<!--${r}-->`,a=RegExp(`${r}|${n}`),i="$lit$";class o{constructor(e,t){this.parts=[],this.element=t;let s=[],n=[],o=document.createTreeWalker(t.content,133,null,!1),c=0,h=-1,p=0,{strings:m,values:{length:g}}=e;for(;p<g;){let e=o.nextNode();if(null===e){o.currentNode=n.pop();continue}if(h++,1===e.nodeType){if(e.hasAttributes()){let t=e.attributes,{length:s}=t,r=0;for(let e=0;e<s;e++)l(t[e].name,i)&&r++;for(;r-- >0;){let t=d.exec(m[p])[2],s=t.toLowerCase()+i,r=e.getAttribute(s);e.removeAttribute(s);let n=r.split(a);this.parts.push({type:"attribute",index:h,name:t,strings:n}),p+=n.length-1}}"TEMPLATE"===e.tagName&&(n.push(e),o.currentNode=e.content)}else if(3===e.nodeType){let t=e.data;if(t.indexOf(r)>=0){let r=e.parentNode,n=t.split(a),o=n.length-1;for(let t=0;t<o;t++){let s;let a=n[t];if(""===a)s=u();else{let e=d.exec(a);null!==e&&l(e[2],i)&&(a=a.slice(0,e.index)+e[1]+e[2].slice(0,-i.length)+e[3]),s=document.createTextNode(a)}r.insertBefore(s,e),this.parts.push({type:"node",index:++h})}""===n[o]?(r.insertBefore(u(),e),s.push(e)):e.data=n[o],p+=o}}else if(8===e.nodeType){if(e.data===r){let t=e.parentNode;(null===e.previousSibling||h===c)&&(h++,t.insertBefore(u(),e)),c=h,this.parts.push({type:"node",index:h}),null===e.nextSibling?e.data="":(s.push(e),h--),p++}else{let t=-1;for(;-1!==(t=e.data.indexOf(r,t+1));)this.parts.push({type:"node",index:-1}),p++}}}for(let e of s)e.parentNode.removeChild(e)}}let l=(e,t)=>{let s=e.length-t.length;return s>=0&&e.slice(s)===t},c=e=>-1!==e.index,u=()=>document.createComment(""),d=/([ \x09\x0a\x0c\x0d])([^\0-\x1F\x7F-\x9F "'>=/]+)([ \x09\x0a\x0c\x0d]*=[ \x09\x0a\x0c\x0d]*(?:[^ \x09\x0a\x0c\x0d"'`<>=]*|"[^"]*|'[^']*))$/}),a("eDnSP",function(t,s){e(t.exports,"TemplateResult",()=>l),e(t.exports,"SVGTemplateResult",()=>c);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("ixdbG"),a=n("8AGqo");let i=window.trustedTypes&&trustedTypes.createPolicy("lit-html",{createHTML:e=>e}),o=` ${a.marker} `;class l{constructor(e,t,s,r){this.strings=e,this.values=t,this.type=s,this.processor=r}getHTML(){let e=this.strings.length-1,t="",s=!1;for(let r=0;r<e;r++){let e=this.strings[r],n=e.lastIndexOf("<!--");s=(n>-1||s)&&-1===e.indexOf("-->",n+1);let i=(0,a.lastAttributeNameRegex).exec(e);null===i?t+=e+(s?o:a.nodeMarker):t+=e.substr(0,i.index)+i[1]+i[2]+a.boundAttributeSuffix+i[3]+a.marker}return t+this.strings[e]}getTemplateElement(){let e=document.createElement("template"),t=this.getHTML();return void 0!==i&&(t=i.createHTML(t)),e.innerHTML=t,e}}class c extends l{getHTML(){return`<svg>${super.getHTML()}</svg>`}getTemplateElement(){let e=super.getTemplateElement(),t=e.content,s=t.firstChild;return t.removeChild(s),(0,r.reparentNodes)(t,s.firstChild),e}}}),a("cFPH9",function(t,s){e(t.exports,"render",()=>l);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("ixdbG"),a=n("3jJxF"),i=n("jZRBD");let o=new WeakMap,l=(e,t,s)=>{let n=o.get(t);void 0===n&&((0,r.removeNodes)(t,t.firstChild),o.set(t,n=new a.NodePart(Object.assign({templateFactory:i.templateFactory},s))),n.appendInto(t)),n.setValue(e),n.commit()}}),a("jZRBD",function(t,s){e(t.exports,"templateFactory",()=>a);/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */var r=n("8AGqo");function a(e){let t=i.get(e.type);void 0===t&&(t={stringsArray:new WeakMap,keyString:new Map},i.set(e.type,t));let s=t.stringsArray.get(e.strings);if(void 0!==s)return s;let n=e.strings.join(r.marker);return void 0===(s=t.keyString.get(n))&&(s=new r.Template(e,e.getTemplateElement()),t.keyString.set(n,s)),t.stringsArray.set(e.strings,s),s}let i=new Map}),a("78UAK",function(t,s){let r;e(t.exports,"default",()=>v);var a=n("aPsVO"),i=n("cFPH9"),o=n("45wDp");let l=o.dataBaseUrl+"stats/games_24h.json",c=new Intl.DateTimeFormat(void 0,{hour:"numeric",minute:"2-digit"}),u=new Intl.DateTimeFormat(void 0,{dateStyle:"medium",timeStyle:"short"}),d=e=>(0,a.html)`
<td class="bot">
    <div>
        <span class="rank ${(0,o.rankcol)(e.rank)}">${e.rank}</span>
        <span class="race-slot" title=${(0,o.racename)(e.race)+(e.randomBot?" (random bot)":"")}>
            ${e.randomBot?(0,o.icon)("dice","race-dice "+(0,o.racecol)(e.race)):(0,a.html)`<span class="race ${(0,o.racecol)(e.race)}"></span>`}
        </span>
        <a class="name${e.winner?" winner":""}" href="/beta/bot.html?bot=${encodeURIComponent(e.name)}" title=${e.name}>${e.name}</a>
        ${e.crashed?(0,a.html)`<span class="crashed" title="Crashed">${(0,o.icon)("car-crash")}</span>`:""}
    </div>
</td>`,h=(e,t,s)=>e?(0,a.html)`<span title=${s}>${(0,o.icon)(t)}</span>`:"",p=(e,t)=>{let[s,r]=e.botB.winner?[e.botB,e.botA]:[e.botA,e.botB],n=new Date(1e3*e.timestamp);return(0,a.html)`
<tr ?hidden=${t}>
    ${d(s)}
    ${d(r)}
    <td class="map" title=${e.map}>${e.map}</td>
    <td class="ended" title=${u.format(n)}>${c.format(n)}</td>
    <td class="time">
        <span class="badges">
            ${h(e.notPlayed,"poo-storm","Did not start: a bot crashed early")}
            ${h(e.realTimeout,"fish","Timed out (real time)")}
            ${h(e.frameTimeout,"stopwatch","Timed out (in-game)")}
            ${h(e.nukes>1,"radiation",e.nukes+" nukes")}
            ${h(e.mm>=400,"biohazard",e.mm+" Marines + Medics")}
            ${h(e.cruisercarrier>=20,"fighter-jet",e.cruisercarrier+" Battlecruisers + Carriers")}
            ${h(e.arbiter>=10,"user-secret",e.arbiter+" Arbiters")}
            ${h(e.lurkers>=100,"spider",e.lurkers+" Lurkers")}
            ${h(e.queens>=80,"chess-queen",e.queens+" Queens")}
            ${h(e.guardians>=10,"pastafarianism",e.guardians+" Guardians")}
            ${h(e.defilers>=10,"smog",e.defilers+" Defilers")}
        </span>
        <span class="duration" title="Game length">${(0,o.icon)("hourglass-half")}</span>${e.gameTime||"–"}
    </td>
    <td class="actions">
        ${e.validGame?(0,a.html)`
        <a href=${"https://dgant.github.io/openbw-replay-viewer/?rep="+encodeURIComponent(s.replayUrl)} target="_blank" title="Watch replay">${(0,o.icon)("eye")}</a>
        <a href=${s.replayUrl} title="Download replay">${(0,o.icon)("download")}</a>`:""}
    </td>
</tr>`},m=(e,t,s)=>(0,a.html)`
<div class="game-filters">
    <input type="search" placeholder="Search bot" .value=${t.search}
        @input=${e=>s({search:e.target.value})}>
    <select @change=${e=>s({map:e.target.value})}>
        <option value="">All maps</option>
        ${e.map(e=>(0,a.html)`<option ?selected=${e===t.map}>${e}</option>`)}
    </select>
</div>`,g=(e,t,s,r,n)=>{let i=s.search.trim().toLowerCase(),o=e=>(!s.map||e.map===s.map)&&(!i||e.botA.name.toLowerCase().includes(i)||e.botB.name.toLowerCase().includes(i)),l=0,c=e.map(e=>o(e)&&(++l<=100||s.showAll));return(0,a.html)`
${n?"":m(t,s,r)}
<table class="games">
    <thead>
        <tr>
            <th colspan="2">Bots</th>
            <th>Map</th>
            <th class="ended">Ended</th>
            <th class="time">Time</th>
            <th class="actions"><span class="sr-only">Replay</span></th>
        </tr>
    </thead>
    <tbody>${e.map((e,t)=>p(e,!c[t]))}</tbody>
</table>
${0===l?(0,a.html)`<p class="no-games">No games match.</p>`:""}
${l>100&&!s.showAll?(0,a.html)`
<button class="show-all" @click=${()=>r({showAll:!0})}>Show all ${l} games</button>`:""}`},f=document.querySelector("#srcRef");f&&(0,i.render)((0,a.html)`Source: <a href=${l}>${l}</a>`,f);var v=function(e){let t=e&&e.filter,s=e&&e.hideFilters,n=document.querySelector("#gamesTable");(r=r||fetch(l).then(e=>e.json())).then(e=>{let r=function(e){let{results:t,maps:s,bots:r}=e;return t.map(e=>{let[t,n]=[e.botA,e.botB].map(e=>{let t=r[e.botIndex];return{name:t.name,winner:e.winner,crashed:e.crashed,race:e.race||t.race,randomBot:e.race&&e.race!==t.race,rank:t.rank}}),a=s[e.mapIndex],i=e.gameEvents&&e.gameEvents.reduce((e,t)=>((e[t.unit]=e[t.unit]||{})[t.event]=t.amount,e),{})||{};return t.replayUrl=o.dataBaseUrl+"bots/"+t.name+"/"+t.name+" vs "+n.name+" "+a+" "+e.gameHash+".rep",n.replayUrl=o.dataBaseUrl+"bots/"+n.name+"/"+n.name+" vs "+t.name+" "+a+" "+e.gameHash+".rep",{botA:t,botB:n,timestamp:e.endedAt,gameTime:e.frameCount?function(e){let t=Math.round(e/24);return Math.floor(t/60)+":"+String(t%60).padStart(2,"0")}(e.frameCount):void 0,notPlayed:!e.frameCount,validGame:!e.invalidGame&&!e.realTimeout,realTimeout:e.realTimeout,frameTimeout:e.frameTimeout,map:a,nukes:i[14]&&i[14][6],mm:(i[0]?i[0][2]:0)+(i[34]?i[34][2]:0),cruisercarrier:(i[72]?i[72][2]:0)+(i[12]?i[12][2]:0),arbiter:i[71]&&i[71][2],lurkers:i[103]&&i[103][2],queens:i[45]&&i[45][2],guardians:i[44]&&i[44][2],defilers:i[46]&&i[46][2]}}).sort((e,t)=>t.timestamp-e.timestamp)}(e);t&&(r=t(r));let a=[...new Set(r.map(e=>e.map))].sort(),l={search:"",map:"",showAll:!1},c=e=>{Object.assign(l,e),(0,i.render)(g(r,a,l,c,s),n)};c({})}).catch(()=>{(0,i.render)((0,a.html)`<p class="no-games">Could not load the games. Please try again later.</p>`,n)})}}),a("45wDp",function(t,s){e(t.exports,"dataBaseUrl",()=>a),e(t.exports,"racecol",()=>i),e(t.exports,"racename",()=>o),e(t.exports,"rankcol",()=>l),e(t.exports,"icon",()=>c);var r=n("aPsVO");let a="https://data.basil-ladder.net/";function i(e){switch(e){case"PROTOSS":case"P":return"race_protoss";case"ZERG":case"Z":return"race_zerg";case"TERRAN":case"T":return"race_terran";case"RANDOM":case"R":return"race_random";default:return"race_unknown"}}function o(e){switch(e){case"PROTOSS":case"P":return"Protoss";case"ZERG":case"Z":return"Zerg";case"TERRAN":case"T":return"Terran";case"RANDOM":case"R":return"Random";default:return"Unknown"}}function l(e){return"rank_"+e.toLowerCase()}let c=(e,t)=>(0,r.html)`<svg class="icon ${t||""}" aria-hidden="true"><use href="/assets/icons.svg#${e}"></use></svg>`});var i=n("aPsVO"),o=n("cFPH9"),l=n("78UAK");(0,l.default)();let c=(0,i.html)`<button class="lucky" @click=${function(){(0,l.default)({hideFilters:!0,filter:e=>(function(e){for(let t=e.length-1;t>0;t--){let s=Math.floor(Math.random()*(t+1)),r=e[t];e[t]=e[s],e[s]=r}return e})(e.filter(e=>!e.botA.crashed&&!e.botB.crashed&&!e.realTimeout)).slice(0,10).map(e=>{if(e.botA.winner=!1,e.botA.loser=!1,e.botB.winner=!1,e.botB.loser=!1,.5>Math.random()){let t=e.botA;e.botA=e.botB,e.botB=t}return e})})}}>I'm feeling lucky</button>`;(0,o.render)(c,document.querySelector("#luckyButton"))})();
//# sourceMappingURL=lastgames.js.map
