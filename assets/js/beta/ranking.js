(()=>{function e(e,t,n,s){Object.defineProperty(e,t,{get:n,set:s,enumerable:!0,configurable:!0})}var t=globalThis,n={},s={},a=t.parcelRequire9829;null==a&&((a=function(e){if(e in n)return n[e].exports;if(e in s){var t=s[e];delete s[e];var a={id:e,exports:{}};return n[e]=a,t.call(a.exports,a,a.exports),a.exports}var r=Error("Cannot find module '"+e+"'");throw r.code="MODULE_NOT_FOUND",r}).register=function(e,t){s[e]=t},t.parcelRequire9829=a);var r=a.register;r("aPsVO",function(t,n){e(t.exports,"html",()=>i),e(t.exports,"render",()=>a("cFPH9").render);/**
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
 */var s=a("iUyab"),r=a("eDnSP");a("iYB8H"),a("ixdbG"),a("gIbhF"),a("3jJxF"),a("cFPH9"),a("jZRBD"),a("lUGvn"),a("8AGqo"),"undefined"!=typeof window&&(window.litHtmlVersions||(window.litHtmlVersions=[])).push("1.4.1");let i=(e,...t)=>new r.TemplateResult(e,t,"html",s.defaultTemplateProcessor)}),r("iUyab",function(t,n){e(t.exports,"defaultTemplateProcessor",()=>r);/**
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
 */var s=a("3jJxF");let r=new class{handleAttributeExpressions(e,t,n,a){let r=t[0];return"."===r?new s.PropertyCommitter(e,t.slice(1),n).parts:"@"===r?[new s.EventPart(e,t.slice(1),a.eventContext)]:"?"===r?[new s.BooleanAttributePart(e,t.slice(1),n)]:new s.AttributeCommitter(e,t,n).parts}handleTextExpression(e){return new s.NodePart(e)}}}),r("3jJxF",function(t,n){e(t.exports,"AttributeCommitter",()=>h),e(t.exports,"NodePart",()=>m),e(t.exports,"BooleanAttributePart",()=>g),e(t.exports,"PropertyCommitter",()=>f),e(t.exports,"EventPart",()=>x);/**
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
 */var s=a("iYB8H"),r=a("ixdbG"),i=a("gIbhF"),l=a("lUGvn"),o=a("eDnSP"),d=a("8AGqo");let c=e=>null===e||!("object"==typeof e||"function"==typeof e),u=e=>Array.isArray(e)||!!(e&&e[Symbol.iterator]);class h{constructor(e,t,n){this.dirty=!0,this.element=e,this.name=t,this.strings=n,this.parts=[];for(let e=0;e<n.length-1;e++)this.parts[e]=this._createPart()}_createPart(){return new p(this)}_getValue(){let e=this.strings,t=e.length-1,n=this.parts;if(1===t&&""===e[0]&&""===e[1]){let e=n[0].value;if("symbol"==typeof e)return String(e);if("string"==typeof e||!u(e))return e}let s="";for(let a=0;a<t;a++){s+=e[a];let t=n[a];if(void 0!==t){let e=t.value;if(c(e)||!u(e))s+="string"==typeof e?e:String(e);else for(let t of e)s+="string"==typeof t?t:String(t)}}return s+e[t]}commit(){this.dirty&&(this.dirty=!1,this.element.setAttribute(this.name,this._getValue()))}}class p{constructor(e){this.value=void 0,this.committer=e}setValue(e){e===i.noChange||c(e)&&e===this.value||(this.value=e,(0,s.isDirective)(e)||(this.committer.dirty=!0))}commit(){for(;(0,s.isDirective)(this.value);){let e=this.value;this.value=i.noChange,e(this)}this.value!==i.noChange&&this.committer.commit()}}class m{constructor(e){this.value=void 0,this.__pendingValue=void 0,this.options=e}appendInto(e){this.startNode=e.appendChild((0,d.createMarker)()),this.endNode=e.appendChild((0,d.createMarker)())}insertAfterNode(e){this.startNode=e,this.endNode=e.nextSibling}appendIntoPart(e){e.__insert(this.startNode=(0,d.createMarker)()),e.__insert(this.endNode=(0,d.createMarker)())}insertAfterPart(e){e.__insert(this.startNode=(0,d.createMarker)()),this.endNode=e.endNode,e.endNode=this.startNode}setValue(e){this.__pendingValue=e}commit(){if(null===this.startNode.parentNode)return;for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}let e=this.__pendingValue;e!==i.noChange&&(c(e)?e!==this.value&&this.__commitText(e):e instanceof o.TemplateResult?this.__commitTemplateResult(e):e instanceof Node?this.__commitNode(e):u(e)?this.__commitIterable(e):e===i.nothing?(this.value=i.nothing,this.clear()):this.__commitText(e))}__insert(e){this.endNode.parentNode.insertBefore(e,this.endNode)}__commitNode(e){this.value!==e&&(this.clear(),this.__insert(e),this.value=e)}__commitText(e){let t=this.startNode.nextSibling,n="string"==typeof(e=null==e?"":e)?e:String(e);t===this.endNode.previousSibling&&3===t.nodeType?t.data=n:this.__commitNode(document.createTextNode(n)),this.value=e}__commitTemplateResult(e){let t=this.options.templateFactory(e);if(this.value instanceof l.TemplateInstance&&this.value.template===t)this.value.update(e.values);else{let n=new l.TemplateInstance(t,e.processor,this.options),s=n._clone();n.update(e.values),this.__commitNode(s),this.value=n}}__commitIterable(e){let t;Array.isArray(this.value)||(this.value=[],this.clear());let n=this.value,s=0;for(let a of e)void 0===(t=n[s])&&(t=new m(this.options),n.push(t),0===s?t.appendIntoPart(this):t.insertAfterPart(n[s-1])),t.setValue(a),t.commit(),s++;s<n.length&&(n.length=s,this.clear(t&&t.endNode))}clear(e=this.startNode){(0,r.removeNodes)(this.startNode.parentNode,e.nextSibling,this.endNode)}}class g{constructor(e,t,n){if(this.value=void 0,this.__pendingValue=void 0,2!==n.length||""!==n[0]||""!==n[1])throw Error("Boolean attributes can only contain a single expression");this.element=e,this.name=t,this.strings=n}setValue(e){this.__pendingValue=e}commit(){for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}if(this.__pendingValue===i.noChange)return;let e=!!this.__pendingValue;this.value!==e&&(e?this.element.setAttribute(this.name,""):this.element.removeAttribute(this.name),this.value=e),this.__pendingValue=i.noChange}}class f extends h{constructor(e,t,n){super(e,t,n),this.single=2===n.length&&""===n[0]&&""===n[1]}_createPart(){return new v(this)}_getValue(){return this.single?this.parts[0].value:super._getValue()}commit(){this.dirty&&(this.dirty=!1,this.element[this.name]=this._getValue())}}class v extends p{}let _=!1;(()=>{try{let e={get capture(){return _=!0,!1}};window.addEventListener("test",e,e),window.removeEventListener("test",e,e)}catch(e){}})();class x{constructor(e,t,n){this.value=void 0,this.__pendingValue=void 0,this.element=e,this.eventName=t,this.eventContext=n,this.__boundHandleEvent=e=>this.handleEvent(e)}setValue(e){this.__pendingValue=e}commit(){for(;(0,s.isDirective)(this.__pendingValue);){let e=this.__pendingValue;this.__pendingValue=i.noChange,e(this)}if(this.__pendingValue===i.noChange)return;let e=this.__pendingValue,t=this.value,n=null==e||null!=t&&(e.capture!==t.capture||e.once!==t.once||e.passive!==t.passive),a=null!=e&&(null==t||n);n&&this.element.removeEventListener(this.eventName,this.__boundHandleEvent,this.__options),a&&(this.__options=b(e),this.element.addEventListener(this.eventName,this.__boundHandleEvent,this.__options)),this.value=e,this.__pendingValue=i.noChange}handleEvent(e){"function"==typeof this.value?this.value.call(this.eventContext||this.element,e):this.value.handleEvent(e)}}let b=e=>e&&(_?{capture:e.capture,passive:e.passive,once:e.once}:e.capture)}),r("iYB8H",function(t,n){e(t.exports,"isDirective",()=>a);/**
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
 */let s=new WeakMap,a=e=>"function"==typeof e&&s.has(e)}),r("ixdbG",function(t,n){e(t.exports,"isCEPolyfill",()=>s),e(t.exports,"reparentNodes",()=>a),e(t.exports,"removeNodes",()=>r);/**
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
 */let s="undefined"!=typeof window&&null!=window.customElements&&void 0!==window.customElements.polyfillWrapFlushCallback,a=(e,t,n=null,s=null)=>{for(;t!==n;){let n=t.nextSibling;e.insertBefore(t,s),t=n}},r=(e,t,n=null)=>{for(;t!==n;){let n=t.nextSibling;e.removeChild(t),t=n}}}),r("gIbhF",function(t,n){e(t.exports,"noChange",()=>s),e(t.exports,"nothing",()=>a);/**
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
 */let s={},a={}}),r("lUGvn",function(t,n){e(t.exports,"TemplateInstance",()=>i);/**
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
 */var s=a("ixdbG"),r=a("8AGqo");class i{constructor(e,t,n){this.__parts=[],this.template=e,this.processor=t,this.options=n}update(e){let t=0;for(let n of this.__parts)void 0!==n&&n.setValue(e[t]),t++;for(let e of this.__parts)void 0!==e&&e.commit()}_clone(){let e;let t=s.isCEPolyfill?this.template.element.content.cloneNode(!0):document.importNode(this.template.element.content,!0),n=[],a=this.template.parts,i=document.createTreeWalker(t,133,null,!1),l=0,o=0,d=i.nextNode();for(;l<a.length;){if(e=a[l],!(0,r.isTemplatePartActive)(e)){this.__parts.push(void 0),l++;continue}for(;o<e.index;)o++,"TEMPLATE"===d.nodeName&&(n.push(d),i.currentNode=d.content),null===(d=i.nextNode())&&(i.currentNode=n.pop(),d=i.nextNode());if("node"===e.type){let e=this.processor.handleTextExpression(this.options);e.insertAfterNode(d.previousSibling),this.__parts.push(e)}else this.__parts.push(...this.processor.handleAttributeExpressions(d,e.name,e.strings,this.options));l++}return s.isCEPolyfill&&(document.adoptNode(t),customElements.upgrade(t)),t}}}),r("8AGqo",function(t,n){e(t.exports,"marker",()=>s),e(t.exports,"nodeMarker",()=>a),e(t.exports,"boundAttributeSuffix",()=>i),e(t.exports,"Template",()=>l),e(t.exports,"lastAttributeNameRegex",()=>u),e(t.exports,"createMarker",()=>c),e(t.exports,"isTemplatePartActive",()=>d);/**
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
 */let s=`{{lit-${String(Math.random()).slice(2)}}}`,a=`<!--${s}-->`,r=RegExp(`${s}|${a}`),i="$lit$";class l{constructor(e,t){this.parts=[],this.element=t;let n=[],a=[],l=document.createTreeWalker(t.content,133,null,!1),d=0,h=-1,p=0,{strings:m,values:{length:g}}=e;for(;p<g;){let e=l.nextNode();if(null===e){l.currentNode=a.pop();continue}if(h++,1===e.nodeType){if(e.hasAttributes()){let t=e.attributes,{length:n}=t,s=0;for(let e=0;e<n;e++)o(t[e].name,i)&&s++;for(;s-- >0;){let t=u.exec(m[p])[2],n=t.toLowerCase()+i,s=e.getAttribute(n);e.removeAttribute(n);let a=s.split(r);this.parts.push({type:"attribute",index:h,name:t,strings:a}),p+=a.length-1}}"TEMPLATE"===e.tagName&&(a.push(e),l.currentNode=e.content)}else if(3===e.nodeType){let t=e.data;if(t.indexOf(s)>=0){let s=e.parentNode,a=t.split(r),l=a.length-1;for(let t=0;t<l;t++){let n;let r=a[t];if(""===r)n=c();else{let e=u.exec(r);null!==e&&o(e[2],i)&&(r=r.slice(0,e.index)+e[1]+e[2].slice(0,-i.length)+e[3]),n=document.createTextNode(r)}s.insertBefore(n,e),this.parts.push({type:"node",index:++h})}""===a[l]?(s.insertBefore(c(),e),n.push(e)):e.data=a[l],p+=l}}else if(8===e.nodeType){if(e.data===s){let t=e.parentNode;(null===e.previousSibling||h===d)&&(h++,t.insertBefore(c(),e)),d=h,this.parts.push({type:"node",index:h}),null===e.nextSibling?e.data="":(n.push(e),h--),p++}else{let t=-1;for(;-1!==(t=e.data.indexOf(s,t+1));)this.parts.push({type:"node",index:-1}),p++}}}for(let e of n)e.parentNode.removeChild(e)}}let o=(e,t)=>{let n=e.length-t.length;return n>=0&&e.slice(n)===t},d=e=>-1!==e.index,c=()=>document.createComment(""),u=/([ \x09\x0a\x0c\x0d])([^\0-\x1F\x7F-\x9F "'>=/]+)([ \x09\x0a\x0c\x0d]*=[ \x09\x0a\x0c\x0d]*(?:[^ \x09\x0a\x0c\x0d"'`<>=]*|"[^"]*|'[^']*))$/}),r("eDnSP",function(t,n){e(t.exports,"TemplateResult",()=>o),e(t.exports,"SVGTemplateResult",()=>d);/**
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
 */var s=a("ixdbG"),r=a("8AGqo");let i=window.trustedTypes&&trustedTypes.createPolicy("lit-html",{createHTML:e=>e}),l=` ${r.marker} `;class o{constructor(e,t,n,s){this.strings=e,this.values=t,this.type=n,this.processor=s}getHTML(){let e=this.strings.length-1,t="",n=!1;for(let s=0;s<e;s++){let e=this.strings[s],a=e.lastIndexOf("<!--");n=(a>-1||n)&&-1===e.indexOf("-->",a+1);let i=(0,r.lastAttributeNameRegex).exec(e);null===i?t+=e+(n?l:r.nodeMarker):t+=e.substr(0,i.index)+i[1]+i[2]+r.boundAttributeSuffix+i[3]+r.marker}return t+this.strings[e]}getTemplateElement(){let e=document.createElement("template"),t=this.getHTML();return void 0!==i&&(t=i.createHTML(t)),e.innerHTML=t,e}}class d extends o{getHTML(){return`<svg>${super.getHTML()}</svg>`}getTemplateElement(){let e=super.getTemplateElement(),t=e.content,n=t.firstChild;return t.removeChild(n),(0,s.reparentNodes)(t,n.firstChild),e}}}),r("cFPH9",function(t,n){e(t.exports,"render",()=>o);/**
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
 */var s=a("ixdbG"),r=a("3jJxF"),i=a("jZRBD");let l=new WeakMap,o=(e,t,n)=>{let a=l.get(t);void 0===a&&((0,s.removeNodes)(t,t.firstChild),l.set(t,a=new r.NodePart(Object.assign({templateFactory:i.templateFactory},n))),a.appendInto(t)),a.setValue(e),a.commit()}}),r("jZRBD",function(t,n){e(t.exports,"templateFactory",()=>r);/**
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
 */var s=a("8AGqo");function r(e){let t=i.get(e.type);void 0===t&&(t={stringsArray:new WeakMap,keyString:new Map},i.set(e.type,t));let n=t.stringsArray.get(e.strings);if(void 0!==n)return n;let a=e.strings.join(s.marker);return void 0===(n=t.keyString.get(a))&&(n=new s.Template(e,e.getTemplateElement()),t.keyString.set(a,n)),t.stringsArray.set(e.strings,n),n}let i=new Map}),r("45wDp",function(t,n){e(t.exports,"dataBaseUrl",()=>r),e(t.exports,"racecol",()=>i),e(t.exports,"racename",()=>l),e(t.exports,"rankcol",()=>o),e(t.exports,"icon",()=>d),e(t.exports,"rankValue",()=>u),e(t.exports,"sortByRank",()=>h);var s=a("aPsVO");let r="https://data.basil-ladder.net/";function i(e){switch(e){case"PROTOSS":case"P":return"race_protoss";case"ZERG":case"Z":return"race_zerg";case"TERRAN":case"T":return"race_terran";case"RANDOM":case"R":return"race_random";default:return"race_unknown"}}function l(e){switch(e){case"PROTOSS":case"P":return"Protoss";case"ZERG":case"Z":return"Zerg";case"TERRAN":case"T":return"Terran";case"RANDOM":case"R":return"Random";default:return"Unknown"}}function o(e){return"rank_"+e.toLowerCase()}let d=(e,t)=>(0,s.html)`<svg class="icon ${t||""}" aria-hidden="true"><use href="/assets/icons.svg#${e}"></use></svg>`,c={S:0,A:1,B:2,C:3,D:4,E:5,F:6};function u(e){return e in c?c[e]:99999}function h(e){e.sort((e,t)=>{if(e.enabled!==t.enabled)return e.enabled?-1:1;let n=e.won+e.lost,s=t.won+t.lost;return n<30&&s>=30?1:s<30&&n>=30?-1:u(e.rank)!==u(t.rank)?u(e.rank)-u(t.rank):e.rating!==t.rating?t.rating-e.rating:e.win7!==t.win7?(t.win7??-1)-(e.win7??-1):t.won-e.won})}});var i=a("aPsVO"),l=a("cFPH9"),o=a("45wDp");let d=o.dataBaseUrl+"stats/ranking.json",c=document.querySelector("#rankingTable"),u=new Intl.NumberFormat(void 0,{style:"percent",minimumFractionDigits:1,maximumFractionDigits:1}),h=new Intl.DateTimeFormat(void 0,{dateStyle:"medium",timeStyle:"short"}),p=new Intl.DateTimeFormat(void 0,{month:"short",day:"numeric"}),m=new Intl.DateTimeFormat(void 0,{dateStyle:"medium"}),g=e=>e.downRanked?(0,i.html)`<span class="rank-change down" title="Rank went down recently">${(0,o.icon)("arrow-down")}</span>`:e.upRanked?(0,i.html)`<span class="rank-change up" title="Rank went up recently">${(0,o.icon)("arrow-up")}</span>`:e.rankProtection?(0,i.html)`<span class="rank-change" title="Rank changed recently and is locked">${(0,o.icon)("lock")}</span>`:"",f=e=>(0,i.html)`
<td class="bot">
    <div>
        <span class="race-slot" title=${(0,o.racename)(e.race)}>${"race_random"===(0,o.racecol)(e.race)?(0,o.icon)("dice","race-dice"):(0,i.html)`<span class="race ${(0,o.racecol)(e.race)}"></span>`}</span>
        <a class="name" href="/beta/bot.html?bot=${encodeURIComponent(e.botName)}" title=${e.botName}>${e.botName}</a>
    </div>
</td>`,v=e=>e.rating?(0,i.html)`<span title="About ${Math.max(100,e.rating-1300)} human MMR">${e.rating}</span>`:(0,i.html)`<span class="pending" title="Needs 30 games">pending</span>`,_=(e,t)=>{var n;return(0,i.html)`
<tr id=${e.botName} class=${e.botName===t?"linked":""}>
    <td class="pos">${e.position}</td>
    <td class="rank-cell"><div><span class="rank ${(0,o.rankcol)(e.rank)}">${e.rank}</span>${g(e)}</div></td>
    ${f(e)}
    <td class="num elo">${v(e)}</td>
    <td class="break"></td>
    <td class="num win" data-label="7d">${null===e.win7?"–":u.format(e.win7)}</td>
    <td class="num wl" data-label="W/L">${e.won} / ${e.lost}</td>
    <td class="num crashes">${e.crashesSinceUpdate>0?(0,i.html)`<span title="Crashes since the last update">${(0,o.icon)("car-crash")} ${e.crashesSinceUpdate}</span>`:""}</td>
    <td class="num updated" title=${e.lastUpdated?h.format(e.lastUpdated):""}>
        ${e.lastUpdated?((n=e.lastUpdated).getFullYear()===new Date().getFullYear()?p:m).format(n):"–"}</td>
</tr>`},x=(e,t)=>(0,i.html)`
<tr id=${e.botName} class=${e.botName===t?"linked":""}>
    ${f(e)}
    <td class="num elo">${v(e)}</td>
    <td class="num wl">${e.won} / ${e.lost}</td>
    <td class="num win">${u.format(e.won/(e.won+e.lost))}</td>
    <td class="num crashes">${e.crashed||""}</td>
    <td class="reason">${e.disabledReason||"Disabled on SSCAIT/locally"}</td>
</tr>`,b=(e,t,n)=>e===t?0:null===e?1:null===t?-1:n*(e<t?-1:1),y=[{key:"pos",label:"#",value:e=>e.position,firstDir:1,title:"Ladder position"},{key:"rank"},{key:"bot",label:"Bot",value:e=>e.botName.toLowerCase(),firstDir:1},{key:"elo",label:"ELO",value:e=>e.rating,firstDir:-1,title:"Hover a value for a rough human MMR estimate"},{key:"break"},{key:"win",label:"Win 7d",value:e=>e.win7,firstDir:-1,title:"Win rate in the last 7 days"},{key:"wl",label:"Won / Lost",value:e=>e.won+e.lost,firstDir:-1,title:"Sorts by number of games"},{key:"crashes",label:"Crashes",value:e=>e.crashesSinceUpdate,firstDir:-1,title:"Crashes since the last update"},{key:"updated",label:"Updated",value:e=>e.lastUpdated&&e.lastUpdated.getTime(),firstDir:-1}],k=(e,t)=>(0,i.html)`
<tr>${y.map(n=>"break"===n.key?(0,i.html)`<th class="break"></th>`:"rank"===n.key?(0,i.html)`<th class="rank-head"><span class="sr-only">Rank</span></th>`:(0,i.html)`
    <th class="${"pos"===n.key||"bot"===n.key?"":"num"} ${e.sort===n.key?"sorted":""}"
        title=${n.title||""} aria-sort=${e.sort===n.key?e.dir>0?"ascending":"descending":"none"}>
        <button @click=${()=>t(e.sort===n.key?{dir:-e.dir}:{sort:n.key,dir:n.firstDir})}>${n.label}${e.sort===n.key?(0,o.icon)(e.dir>0?"arrow-up":"arrow-down","sort-arrow"):""}</button>
    </th>`)}
</tr>`,w=(e,t,n,s)=>{let a=y.find(e=>e.key===n.sort),r=e.slice().sort((e,t)=>b(a.value(e),a.value(t),n.dir));return(0,i.html)`
<table class="ranking">
    <thead>${k(n,s)}</thead>
    <tbody>${r.map(e=>_(e,n.linked))}</tbody>
</table>
<details class="disabled-bots" ?open=${t.some(e=>e.botName===n.linked)}>
    <summary>Disabled bots (${t.length})</summary>
    <table class="ranking disabled">
        <thead><tr><th>Bot</th><th class="num">ELO</th><th class="num">Won / Lost</th><th class="num">Win</th>
            <th class="num">Crashes</th><th>Reason</th></tr></thead>
        <tbody>${t.map(e=>x(e,n.linked))}</tbody>
    </table>
</details>`};(0,l.render)((0,i.html)`Source: <a href=${d}>${d}</a>`,document.querySelector("#srcRef")),fetch(d).then(e=>e.json()).then(e=>{for(let t of e){"UNRANKED"===t.rank&&(t.rank="U"),"UNRANKED"===t.previousRank&&(t.previousRank="U");let e=t.wonInWindow+t.lostInWindow;t.win7=e>0?t.wonInWindow/e:null,t.won+t.lost<30&&(t.rating=null),t.lastUpdated=t.lastUpdated>0?new Date(1e3*t.lastUpdated):null;let n=(0,o.rankValue)(t.rank)-(0,o.rankValue)(t.previousRank);t.downRanked=t.previousRank&&n>0,t.upRanked=t.previousRank&&n<0}(0,o.sortByRank)(e);let t=e.filter(e=>e.enabled);t.forEach((e,t)=>e.position=t+1);let n=e.filter(e=>!e.enabled&&e.won+e.lost>0),s={sort:"pos",dir:1,linked:function(){try{return decodeURIComponent(location.hash.substring(1))}catch(e){return""}}()},a=e=>{Object.assign(s,e),(0,l.render)(w(t,n,s,a),c)};a({});let r=s.linked&&document.getElementById(s.linked);r&&r.scrollIntoView({block:"center"})}).catch(e=>{console.error(e),(0,l.render)((0,i.html)`<p class="no-games">Could not load the ranking. Please try again later.</p>`,c)})})();
//# sourceMappingURL=ranking.js.map
