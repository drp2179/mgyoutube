import { Request, Response, Next, Server } from 'restify'


export class UseNext {
    public nextParameters: any;

    constructor(nextParameters?: any) {
        this.nextParameters = nextParameters;
    }

    public static Nothing: UseNext = new UseNext();

    public static handleAsyncRestifyCall(useNext: UseNext, next: Next): any {
        if (useNext.nextParameters) {
            return next(useNext.nextParameters);
        } else {
            return next();
        }
    }
}

// export function handleAsyncRestifyCallExport(useNext: UseNext, next: Next): any {
//     if (useNext.nextParameters) {
//         return next(useNext.nextParameters);
//     } else {
//         return next();
//     }

// }

